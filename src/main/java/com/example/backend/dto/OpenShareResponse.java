package com.example.backend.dto;

import java.time.Instant;

import com.example.backend.entity.Share;

// パスワードが合っていて、有効期限内のときだけ返す、共有されたWebサイトの情報
//   埋め込めるサイト：embedUrl が入る（url は null）。公開ページの中に iframe で表示する
//   埋め込めないサイト：url が入る（embedUrl は null）。そのサイトへ移動する
// 埋め込めるときに元のURLを返さないのは、アドレスをなるべく目に触れさせないため
public record OpenShareResponse(String url, String embedUrl, String title, Instant closesAt) {

	public static OpenShareResponse from(Share share) {
		return share.isEmbeddable()
				? new OpenShareResponse(null, share.getEmbedUrl(), share.getTitle(), share.getClosesAt())
				: new OpenShareResponse(share.getUrl(), null, share.getTitle(), share.getClosesAt());
	}
}
