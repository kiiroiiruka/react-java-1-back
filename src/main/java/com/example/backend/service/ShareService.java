package com.example.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.OpenShareResponse;
import com.example.backend.dto.PublicShareResponse;
import com.example.backend.dto.ShareRequest;
import com.example.backend.dto.ShareResponse;
import com.example.backend.entity.Share;
import com.example.backend.entity.User;
import com.example.backend.repository.ShareRepository;
import com.example.backend.repository.UserRepository;

// 共有リンクの作成・一覧を担当する
@Service
public class ShareService {

	// BCryptが扱えるパスワードの最大バイト数（日本語は1文字3バイト）
	private static final int MAX_PASSWORD_BYTES = 72;

	// 推測できないランダムな値を作るための道具
	private static final SecureRandom RANDOM = new SecureRandom();

	// タイムゾーンが指定されなかったときは日本時間で判定する
	// （Herokuのサーバーの時計はUTCなので、何も指定しないと日本の朝9時まで日付が1日ずれる）
	private static final String DEFAULT_TIME_ZONE = "Asia/Tokyo";

	private final ShareRepository shareRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public ShareService(ShareRepository shareRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.shareRepository = shareRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// userId は JWT から取り出したもの（= ログイン中の本人）
	// フロントから送られてきた値ではないので、他人になりすまして保存することはできない
	@Transactional
	public ShareResponse create(Long userId, ShareRequest req) {
		if (req.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "パスワードが長すぎます");
		}
		// 「どの国の時刻で期限を判定するか」。過去の日付かどうかも、そのタイムゾーンの今日で比べる
		ZoneId zone = parseZone(req.timeZone());
		if (req.expiresAt().isBefore(LocalDate.now(zone))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "有効期限は今日以降にしてください");
		}
		User user = findUser(userId);
		Share share = new Share(
				user,
				req.url().trim(),
				req.title().trim(),
				req.recipientName().trim(),
				req.recipientEmail().trim(),
				req.message(),
				// 共有リンクのパスワードも、ユーザーのパスワードと同じくハッシュ化してから保存する
				passwordEncoder.encode(req.password()),
				req.expiresAt(),
				zone.getId(),
				newSlug());
		return ShareResponse.from(shareRepository.save(share));
	}

	// ログイン中の本人が作った共有リンクだけを、新しい順に返す
	@Transactional(readOnly = true)
	public List<ShareResponse> list(Long userId) {
		return shareRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(ShareResponse::from)
				.toList();
	}

	// ---- ここから下は公開ページ（/s/{slug}）用。ログインしていない人も呼ぶ ----

	// 公開ページを開いたとき：リンクが存在して、期限内かだけを確認する
	// 存在しなければ404、期限切れなら410（Gone：前は使えたけど、もう使えない）
	@Transactional(readOnly = true)
	public PublicShareResponse getPublic(String slug) {
		Share share = findAvailable(slug);
		return new PublicShareResponse(share.getExpiresAt(), share.getTimeZone());
	}

	// パスワードが入力されたとき：合っていれば、共有されたURLを返す
	@Transactional(readOnly = true)
	public OpenShareResponse open(String slug, String password) {
		Share share = findAvailable(slug);
		// ログインと同じく、入力されたパスワードと保存してあるハッシュをBCryptで比べる
		if (!passwordEncoder.matches(password, share.getPasswordHash())) {
			// ここは401ではなく403にする（401はフロントで「ログインが切れた」扱いになるため）
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "パスワードが違います");
		}
		return new OpenShareResponse(share.getUrl(), share.getTitle());
	}

	private Share findAvailable(String slug) {
		Share share = shareRepository.findBySlug(slug)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "リンクが見つかりません"));
		if (share.isExpired()) {
			throw new ResponseStatusException(HttpStatus.GONE, "このリンクは有効期限が切れています");
		}
		return share;
	}

	// "Asia/Tokyo" のような名前（IANAタイムゾーンID）を ZoneId に変換する
	// 空なら日本時間、存在しない名前なら400
	private ZoneId parseZone(String timeZone) {
		if (timeZone == null || timeZone.isBlank()) {
			return ZoneId.of(DEFAULT_TIME_ZONE);
		}
		try {
			return ZoneId.of(timeZone);
		} catch (DateTimeException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "タイムゾーンが正しくありません");
		}
	}

	// JWTは本物でも、その後ユーザーが削除されていることがあるので、そのときは401
	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	// 9バイトのランダムな値を、URLに入れても大丈夫な12文字にする（例：k3Jx9qLm_aB2）
	private String newSlug() {
		byte[] bytes = new byte[9];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
