package com.example.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.entity.User;
//DIコンテナ経由ではないため以下のようにimportする必要がある
import com.example.backend.repository.UserRepository;

//@Service 「このクラスは実際の処理を担当する」と宣言する。コントローラーから呼ばれる
@Service
public class AuthService {

	// BCryptが扱えるパスワードの最大バイト数（日本語は1文字3バイト）
	private static final int MAX_PASSWORD_BYTES = 72;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	/*
	PasswordConfig                  DI コンテナ                 AuthService
	@Bean passwordEncoder()  ──▶  BCrypt の実物を保管
										│
										│  Spring が「PasswordEncoder 型が欲しいのか」
										│  → 保管庫から探して渡す
										▼
								public AuthService(PasswordEncoder passwordEncoder)
								「PasswordEncoder 型のものをください」と言っているだけ
	*/
	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public AuthResponse register(AuthRequest req) {
		String email = normalize(req.email());
		if (tooLong(req.password())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "パスワードが長すぎます");
		}
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "このメールアドレスは登録済みです");
		}

		// パスワードは平文で保存せず、ハッシュ化してから保存する
		User user = new User(email, passwordEncoder.encode(req.password()));
		try {
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			// 同時に同じメールで登録された場合（DBの重複禁止制約に引っかかった）
			throw new ResponseStatusException(HttpStatus.CONFLICT, "このメールアドレスは登録済みです");
		}
		return new AuthResponse(email, "登録しました");
	}

	public AuthResponse login(AuthRequest req) {
		if (req.email() == null || req.password() == null || tooLong(req.password())) {
			throw unauthorized();
		}
		User user = userRepository.findByEmail(normalize(req.email()))
				.orElseThrow(this::unauthorized);

		// 入力されたパスワードと、保存してあるハッシュが一致するかを確認する
		if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
			throw unauthorized();
		}
		return new AuthResponse(user.getEmail(), "ログインしました");
	}

	// " A@Example.com " と "a@example.com" を同じメールとして扱う
	private String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private boolean tooLong(String password) {
		return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います");
	}
}
