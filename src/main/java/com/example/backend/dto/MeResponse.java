package com.example.backend.dto;

// GET /api/me で返す、ログイン中のユーザーの情報 { "id": 1, "email": "..." }
public record MeResponse(Long id, String email) {
}
