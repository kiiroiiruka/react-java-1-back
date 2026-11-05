package com.example.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.AuthResponse;
import com.example.backend.entity.EmailVerificationToken;
import com.example.backend.entity.User;
import com.example.backend.repository.EmailVerificationTokenRepository;

// メール認証の処理を担当する
//   1. sendVerificationMail … 登録直後に呼ばれる。トークンを作ってメールを送る
//   2. verify               … メール内のリンクが開かれたときに呼ばれる。トークンを確認する
@Service
public class EmailVerificationService {

	// リンクの有効期限（24時間）
	private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);

	// 推測できないランダムな値を作るための道具（普通の Random は予想されやすいのでダメ）
	private static final SecureRandom RANDOM = new SecureRandom();

	private final EmailVerificationTokenRepository tokenRepository;
	// spring-boot-starter-mail を入れると、Springがメール送信の道具を用意してくれる
	private final JavaMailSender mailSender;
	private final String frontendUrl;

	// @Value application.properties の app.frontend-url の値を受け取る
	public EmailVerificationService(
			EmailVerificationTokenRepository tokenRepository,
			JavaMailSender mailSender,
			@Value("${app.frontend-url}") String frontendUrl) {
		this.tokenRepository = tokenRepository;
		this.mailSender = mailSender;
		this.frontendUrl = frontendUrl;
	}

	public void sendVerificationMail(User user) {
		// 生のトークン（メールのリンクに入れる）と、そのハッシュ（DBに保存する）
		String rawToken = newRawToken();
		Instant expiresAt = Instant.now().plus(TOKEN_LIFETIME);
		tokenRepository.save(new EmailVerificationToken(user, hash(rawToken), expiresAt));

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom("no-reply@react-java-1.local");
		mail.setTo(user.getEmail());
		mail.setSubject("【react-java-1】メールアドレスの確認");
		mail.setText("""
				ご登録ありがとうございます。
				下のリンクを開いて、メールアドレスの確認を完了してください。

				%s/verify?token=%s

				このリンクの有効期限は24時間です。
				""".formatted(frontendUrl, rawToken));
		mailSender.send(mail);
	}

	// @Transactional この中のDB操作を「全部成功」か「全部なかったこと」のどちらかにする
	// さらに、読み込んだEntityを書き換えるだけで、メソッドの最後に自動でDBに保存される
	@Transactional
	public AuthResponse verify(String rawToken) {
		// 届いたトークンをハッシュ化してから探す（DBにはハッシュしか入っていないため）
		EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "このリンクは無効です"));

		if (token.isUsed()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "このリンクは使用済みです");
		}
		if (token.isExpired()) {
			// 410 Gone：前は使えたけど、もう使えない
			throw new ResponseStatusException(HttpStatus.GONE, "このリンクは有効期限が切れています");
		}

		// users.email_verified_at と tokens.used_at に今の日時を入れる
		User user = token.getUser();
		user.markEmailVerified();
		token.markUsed();
		return new AuthResponse(user.getEmail(), "メールアドレスを確認しました");
	}

	// 32バイトのランダムな値を、URLに入れても大丈夫な文字列にする
	private String newRawToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	// SHA-256 でハッシュ化する
	// パスワードと違い、トークンは十分ランダムで長いので、BCryptではなく速いSHA-256で十分
	// （BCryptは毎回結果が変わるので、WHERE token_hash = ? で探せない）
	private String hash(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 はJavaに必ず入っているので、ここには来ない
			throw new IllegalStateException(e);
		}
	}
}
