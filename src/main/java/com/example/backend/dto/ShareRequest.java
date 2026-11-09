package com.example.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// フロントから送られてくる、共有リンク作成のデータ
// { "url": "...", "title": "...", "recipientName": "...", "recipientEmail": "...",
//   "message": "...", "password": "...", "expiresAt": "2026-12-31" } に対応
// コントローラーで @Valid を付けると、下のチェックに通らない場合は自動で 400 を返す
public record ShareRequest(
		// http:// か https:// で始まるものだけ（javascript: などの危ないURLを保存させない）
		@NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://.+") String url,
		@NotBlank @Size(max = 255) String title,
		@NotBlank @Size(max = 255) String recipientName,
		@NotBlank @Email String recipientEmail,
		@NotBlank @Size(max = 1000) String message,
		@NotBlank @Size(min = 4, max = 72) String password,
		// 過去の日付はNG（"2026-12-31" の形の文字列が LocalDate に変換される）
		@NotNull @FutureOrPresent LocalDate expiresAt) {
}
