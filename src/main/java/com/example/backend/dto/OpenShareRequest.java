package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;

// 公開ページでパスワードを入力したときに送られてくる { "password": "..." }
public record OpenShareRequest(@NotBlank String password) {
}
