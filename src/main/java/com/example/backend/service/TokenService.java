package com.example.backend.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.example.backend.entity.User;

// ログインに成功したユーザーに、JWT（ハンコつきの身分証明書）を発行する
@Service
public class TokenService {

	// JWTは一度発行すると取り消せないので、有効期限は短めにする
	private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

	private final JwtEncoder jwtEncoder;

	public TokenService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public String issue(User user) {
		Instant now = Instant.now();
		// JWTの中身（誰でも読めるので、パスワードなどの秘密は入れない）
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.subject(String.valueOf(user.getId())) // sub：誰のJWTか（ユーザーID）
				.claim("email", user.getEmail())
				.issuedAt(now) // iat：発行した日時
				.expiresAt(now.plus(TOKEN_LIFETIME)) // exp：有効期限
				.build();
		// ヘッダー：HS256 方式でハンコを押す
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
