package com.example.backend.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

// JWT（ハンコつきの身分証明書）を「作る道具」と「確かめる道具」をDIコンテナに登録する
//
// ハンコ（署名）には HS256 という方式を使う。
// 同じ秘密の鍵で「ハンコを押す」のも「ハンコが本物か確かめる」のも行う。
// この鍵が漏れると誰でも偽物のJWTを作れてしまうので、本番では環境変数 JWT_SECRET で渡す。
@Configuration
public class JwtConfig {

	// HS256 の鍵は 256ビット（32バイト）以上必要
	private static final int MIN_SECRET_BYTES = 32;

	private final SecretKey secretKey;

	// @Value application.properties の app.jwt.secret の値を受け取る
	public JwtConfig(@Value("${app.jwt.secret}") String secret) {
		byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
		// 短すぎる鍵だとJWTを作る瞬間にエラーになるので、起動時に気づけるようにする
		if (bytes.length < MIN_SECRET_BYTES) {
			throw new IllegalStateException("app.jwt.secret は32バイト以上にしてください");
		}
		this.secretKey = new SecretKeySpec(bytes, "HmacSHA256");
	}

	// JWTを作る道具（ログイン成功時に TokenService が使う）
	@Bean
	public JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
	}

	// JWTを確かめる道具（リクエストのたびに Spring Security が使う）
	// ハンコが本物か・有効期限が切れていないかをチェックしてくれる
	@Bean
	public JwtDecoder jwtDecoder() {
		return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
	}
}
