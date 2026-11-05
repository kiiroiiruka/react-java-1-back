package com.example.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
	private final EmailVerificationService emailVerificationService;
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
	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
			EmailVerificationService emailVerificationService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.emailVerificationService = emailVerificationService;
	}

	//エラーが怒ったらその処理はなかったことにしようとするのが@Transactional。
	// @Transactional メール送信に失敗したら、ユーザーの保存も取り消す
	// （取り消さないと「登録済みなのにメールが来ない」状態になり、同じメールで登録し直せなくなる）
	@Transactional
	public AuthResponse register(AuthRequest req) {
		//フロントから届いたメールを決まったか形に整える
		String email = normalize(req.email());
		//パスワードが長すぎる場合は400を返す
		if (tooLong(req.password())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "パスワードが長すぎます");
		}
		//同じメールで登録済みなら400を返す
		if (userRepository.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "このメールアドレスは登録済みです");
		}
		// パスワードは平文で保存せず、ハッシュ化してから保存する
		User user = new User(email, passwordEncoder.encode(req.password()));
		// ここでDBに保存する。保存に失敗したら例外が飛ぶので、呼び出し元のコントローラーで400を返す
		try {
			//データ保持
			userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			// 同時に同じメールで登録された場合（DBの重複禁止制約に引っかかった）
			throw new ResponseStatusException(HttpStatus.CONFLICT, "このメールアドレスは登録済みです");
		}
		// この時点ではまだ仮登録。確認メールのリンクが開かれたら本登録になる
		// ここで仮登録を行っている。
		emailVerificationService.sendVerificationMail(user);
		//フロントに返事を返す。この際はDTOを使う。
		return new AuthResponse(email, "確認メールを送信しました");
	}

	public AuthResponse login(AuthRequest req) {
		if (req.email() == null || req.password() == null || tooLong(req.password())) {
			throw unauthorized();
		}
		// フロントから届いたメールを決まったか形に整える
		User user = userRepository.findByEmail(normalize(req.email()))
				.orElseThrow(this::unauthorized);

		// 入力されたパスワードと、保存してあるハッシュが一致するかを確認する
		if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
			throw unauthorized();
		}
		// パスワードが合っていても、メールの確認が終わっていなければログインさせない
		// （401ではなく403にするのは、本人だと分かったうえで「まだダメ」と伝えるため）
		if (!user.isEmailVerified()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "メールアドレスの確認がまだです");
		}
		//フロントエンドに返すデータの型をDTOのAuthResponseにする。DTOはフロントエンドに返すデータの型を定義するためのもの。
		return new AuthResponse(user.getEmail(), "ログインしました");
	}

	// " A@Example.com " と "a@example.com" を同じメールとして扱う
	//メールアドレスを決まった形に揃えるメソッド
	//メールアドレスは大文字小文字「〇〇@example.com」で〇〇の部分は
	//大文字で書かれていても小文字で書かれていても同じメールアドレス扱いになる。
	private String normalize(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
	
	//ハッシュ化させる際にパスワードが長すぎるとBCryptがエラーを出すので、
	//バイト計算で長さを図り、もしもpasswordが長すぎる場合は400を返す
	private boolean tooLong(String password) {
		return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
	}
	//ログイン処理で失敗した際に起動させるメソッド
	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "メールアドレスまたはパスワードが違います");
	}
}
