package com.example.backend.service;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

// 共有されたURLを、公開ページの中に埋め込んで（iframeで）表示できるかを調べる
//
// 多くのサイトは「ほかのサイトに埋め込まれること」を禁止している（クリックジャッキング対策）。
// 禁止しているかどうかは、そのサイトが返すヘッダーで分かる:
//   X-Frame-Options: DENY / SAMEORIGIN            → 埋め込み禁止
//   Content-Security-Policy: frame-ancestors ...  → 書かれたサイト以外は埋め込み禁止
// ブラウザ（React）からは他のサイトのヘッダーを読めないので、サーバーが代わりに見に行く
@Service
public class EmbedService {

	private static final Duration TIMEOUT = Duration.ofSeconds(5);
	private static final int MAX_REDIRECTS = 3;

	// YouTubeの動画URL（watch?v= / youtu.be/ / shorts/ / embed/）から動画IDを取り出す
	private static final Pattern YOUTUBE = Pattern.compile(
			"^https?://(?:www\\.|m\\.)?(?:youtube\\.com/(?:watch\\?(?:.*&)?v=|shorts/|embed/)|youtu\\.be/)([A-Za-z0-9_-]{11})");

	// リダイレクトは自分で1回ずつ追いかける（行き先が内部のアドレスでないか毎回チェックするため）
	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();

	// 埋め込める場合は iframe に入れるURLを返す。埋め込めない・分からない場合は空
	public Optional<String> findEmbedUrl(String url) {
		// YouTubeの動画は、埋め込み専用のURLに変換すれば埋め込める
		Matcher youtube = YOUTUBE.matcher(url);
		if (youtube.find()) {
			return Optional.of("https://www.youtube.com/embed/" + youtube.group(1));
		}
		try {
			URI uri = URI.create(url);
			for (int i = 0; i <= MAX_REDIRECTS; i++) {
				// SSRF対策：サーバーの内部（localhost や 192.168.x.x など）には絶対にアクセスしない
				if (!isPublicHost(uri.getHost())) {
					return Optional.empty();
				}
				HttpResponse<Void> res = httpClient.send(
						HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build(),
						HttpResponse.BodyHandlers.discarding());
				int status = res.statusCode();
				if (status >= 300 && status < 400) {
					Optional<String> location = res.headers().firstValue("Location");
					if (location.isEmpty()) {
						return Optional.empty();
					}
					uri = uri.resolve(location.get());
					continue;
				}
				// ページが開けないなら、埋め込めるかも分からないので「埋め込めない」扱い
				if (status >= 400) {
					return Optional.empty();
				}
				return allowsFraming(res) ? Optional.of(url) : Optional.empty();
			}
			return Optional.empty();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return Optional.empty();
		} catch (Exception e) {
			// タイムアウト・存在しないドメイン・おかしなURLなど。調べられなければ「埋め込めない」扱い
			return Optional.empty();
		}
	}

	private boolean allowsFraming(HttpResponse<?> res) {
		if (res.headers().firstValue("X-Frame-Options").isPresent()) {
			return false;
		}
		for (String csp : res.headers().allValues("Content-Security-Policy")) {
			for (String directive : csp.split(";")) {
				String d = directive.trim().toLowerCase(Locale.ROOT);
				// frame-ancestors * だけは「どのサイトにも埋め込んでいい」という意味
				if (d.startsWith("frame-ancestors") && !d.equals("frame-ancestors *")) {
					return false;
				}
			}
		}
		return true;
	}

	// ホスト名のIPアドレスが、インターネット上の普通のアドレスかを確認する
	private boolean isPublicHost(String host) throws Exception {
		if (host == null) {
			return false;
		}
		for (InetAddress address : InetAddress.getAllByName(host)) {
			byte[] ip = address.getAddress();
			boolean uniqueLocalV6 = ip.length == 16 && (ip[0] & 0xFE) == 0xFC; // fc00::/7
			if (address.isLoopbackAddress() // 127.0.0.1 など（自分自身）
					|| address.isSiteLocalAddress() // 10.x / 172.16-31.x / 192.168.x
					|| address.isLinkLocalAddress() // 169.254.x（クラウドの内部情報が取れる場所）
					|| address.isAnyLocalAddress() // 0.0.0.0
					|| address.isMulticastAddress()
					|| uniqueLocalV6) {
				return false;
			}
		}
		return true;
	}
}
