package com.example.backend.dto;

import java.time.LocalDate;

// 公開ページを開いたとき（パスワード入力前）に返す情報
// 共有先のURLやタイトルは、パスワードが合うまで返さない
// 「12/31まで（日本時間）」のように表示するため、タイムゾーンも返す
public record PublicShareResponse(LocalDate expiresAt, String timeZone) {
}
