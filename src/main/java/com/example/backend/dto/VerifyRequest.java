package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

// フロントから送られてくる { "token": "..." } に対応（メール内リンクの ?token= の部分）
public record VerifyRequest(@NotBlank String token) {
}
