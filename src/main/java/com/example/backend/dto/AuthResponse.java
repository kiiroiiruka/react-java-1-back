//バック→フロントへの信号
package com.example.backend.dto;

// フロントに返す { "email": "...", "message": "..." } に対応
public record AuthResponse(String email, String message) {
}
