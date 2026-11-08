package com.example.backend.dto;

// ログイン成功時にフロントに返す { "email": "...", "token": "...", "message": "..." } に対応
// token は JWT。フロントはこれを保存して、以降のリクエストのヘッダーにつける
public record LoginResponse(String email, String token, String message) {
}
