package com.example.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.VerifyRequest;
import com.example.backend.service.AuthService;
import com.example.backend.service.EmailVerificationService;

import jakarta.validation.Valid;

//@RequestMapping このクラスのAPIは全部 /api/auth から始まる
@RestController // このクラスはAPI用。戻り値はJSONで返す
@RequestMapping("/api/auth") // URLの共通部分。下の "/login" などとつながって /api/auth/login になる

public class AuthController {

	private final AuthService authService;
	private final EmailVerificationService emailVerificationService;

	// Springが自動でAuthServiceなどを渡してくれる（DI：依存性の注入）
	public AuthController(AuthService authService, EmailVerificationService emailVerificationService) {
		this.authService = authService;
		this.emailVerificationService = emailVerificationService;
	}

	//新規登録
	// POST /api/auth/register  @RequestBody 送られてきたJSONをAuthRequestに変換して受け取る
	// @Valid AuthRequestに書いたチェック（メール形式・8文字以上など）を実行し、NGなら自動で400を返す
	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody AuthRequest req) {
		return authService.register(req);
	}

	// POST /api/auth/login
	@PostMapping("/login")
	public AuthResponse login(@RequestBody AuthRequest req) {
		return authService.login(req);
	}

	// POST /api/auth/verify  メール内のリンクから開いた画面（VerifyEmailPage）が呼ぶ
	// 成功なら200、無効・使用済みなら400、期限切れなら410（判定は EmailVerificationService）
	@PostMapping("/verify")
	public AuthResponse verify(@Valid @RequestBody VerifyRequest req) {
		return emailVerificationService.verify(req.token());
	}
}
