package com.example.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// email_verification_tokens テーブルに対応する。1つのインスタンス = 確認メール1通分の鍵
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 「このトークンは誰のものか」を表す紐（FK → users の id）
	// @ManyToOne 1人のユーザーに対して、トークンは何個あってもいい（多対1）
	// DBには user_id という列ができて、users の id が入る
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	// 生のトークンではなく、ハッシュだけを保存する（DBが盗まれてもリンクを作れないように）
	@Column(nullable = false, unique = true)
	private String tokenHash;

	// 有効期限。これを過ぎたら使えない
	@Column(nullable = false)
	private Instant expiresAt;

	// 使った日時。まだ使っていなければ null。同じリンクを2回使えないようにする
	private Instant usedAt;

	@Column(nullable = false)
	private Instant createdAt;

	// JPAがDBから読み込むときに使う空のコンストラクタ（必須）
	protected EmailVerificationToken() {
	}

	public EmailVerificationToken(User user, String tokenHash, Instant expiresAt) {
		this.user = user;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	public User getUser() {
		return user;
	}

	public boolean isUsed() {
		return usedAt != null;
	}

	// 今の時刻が有効期限を過ぎているか
	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public void markUsed() {
		this.usedAt = Instant.now();
	}
}
