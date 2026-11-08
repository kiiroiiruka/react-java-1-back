package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// Spring Security の設定。「どのURLはログインなしでOKで、どのURLはJWTが必要か」を決める
//
// リクエストの流れ:
//   ブラウザ → [Spring Security（ここでJWTをチェック）] → Controller → Service → Repository → DB
// JWTがない・偽物・期限切れなら、Controllerまで届く前に 401 を返す
@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CORS は WebConfig の設定（許可するフロントのURL）をそのまま使う
			.cors(Customizer.withDefaults())
			// CSRF対策は、Cookieを自動で送る方式のためのもの。
			// JWTはReactが自分でヘッダーにつけるので、CSRF攻撃は成り立たない → オフにする
			.csrf(csrf -> csrf.disable())
			// サーバーは「誰がログイン中か」を覚えない（毎回JWTで確かめる）
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				// 新規登録・ログイン・メール確認は、まだJWTを持っていない人が使うのでOKにする
				// /error はエラー時にSpringが内部で使うURL。ここをふさぐと 409 などが全部 401 に化けてしまう
				.requestMatchers("/api/auth/**", "/api/hello", "/error").permitAll()
				// それ以外のAPIは全部JWTが必要
				.anyRequest().authenticated())
			// 「Authorization: Bearer <JWT>」ヘッダーを読んで、JwtConfig の jwtDecoder でチェックする
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}
}
