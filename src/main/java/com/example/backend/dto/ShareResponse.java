package com.example.backend.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.example.backend.entity.Share;

// フロントに返す共有リンクの情報
// パスワード（のハッシュ）は返さない。Entityをそのまま返さずDTOを挟むのはこのため
public record ShareResponse(
		Long id,
		String url,
		// 公開ページの中に埋め込んで表示できるか（false なら、相手はサイトへ移動する）
		boolean embeddable,
		String title,
		String recipientName,
		String recipientEmail,
		String message,
		LocalDate expiresAt,
		String timeZone,
		// 有効期限が切れているか（一覧で「期限切れ」と表示するため）
		boolean expired,
		String slug,
		Instant createdAt) {

	// Entity（DBの1行）→ DTO（フロントに返す形）に詰め替える
	public static ShareResponse from(Share share) {
		return new ShareResponse(
				share.getId(),
				share.getUrl(),
				share.isEmbeddable(),
				share.getTitle(),
				share.getRecipientName(),
				share.getRecipientEmail(),
				share.getMessage(),
				share.getExpiresAt(),
				share.getTimeZone(),
				share.isExpired(),
				share.getSlug(),
				share.getCreatedAt());
	}
}
