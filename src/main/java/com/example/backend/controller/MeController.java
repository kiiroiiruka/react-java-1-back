package com.example.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.MeResponse;
import com.example.backend.service.AuthService;

// ログイン中のユーザー自身の情報を返すAPI
// SecurityConfig で /api/auth/** 以外はJWTが必要にしているので、ここにはJWTが本物の人しか来ない
@RestController
public class MeController {

	private final AuthService authService;

	public MeController(AuthService authService) {
		this.authService = authService;
	}

	// GET /api/me
	// @AuthenticationPrincipal Spring Security がチェック済みのJWTの中身を渡してくれる
	@GetMapping("/api/me")
	public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
		// jwt.getSubject() は TokenService で入れた sub（ユーザーID）
		return authService.me(Long.valueOf(jwt.getSubject()));
	}
}
