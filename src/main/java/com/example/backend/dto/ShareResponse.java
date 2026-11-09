package com.example.backend.dto;

import java.time.Instant;
import java.time.LocalDate;

import com.example.backend.entity.Share;

// フロントに返す共有リンクの情報
// パスワード（のハッシュ）は返さない。Entityをそのまま返さずDTOを挟むのはこのため
public record ShareResponse(
		Long id,
		String url,
		String title,
		String recipientName,
		String recipientEmail,
		String message,
		LocalDate expiresAt,
		String timeZone,
		String slug,
		Instant createdAt) {

	// Entity（DBの1行）→ DTO（フロントに返す形）に詰め替える
	public static ShareResponse from(Share share) {
		return new ShareResponse(
				share.getId(),
				share.getUrl(),
				share.getTitle(),
				share.getRecipientName(),
				share.getRecipientEmail(),
				share.getMessage(),
				share.getExpiresAt(),
				share.getTimeZone(),
				share.getSlug(),
				share.getCreatedAt());
	}
}
