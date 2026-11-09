package com.example.backend.dto;

// パスワードが合っていて、有効期限内のときだけ返す、共有されたWebサイトの情報
public record OpenShareResponse(String url, String title) {
}
