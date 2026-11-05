package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.EmailVerificationToken;

// UserRepository と同じく、interfaceを書くだけでSpringが中身（SQL）を作ってくれる
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

	// SELECT * FROM email_verification_tokens WHERE token_hash = ? が自動で作られる
	Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
}
