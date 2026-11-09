package com.example.backend.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// shares テーブルに対応する。1つのインスタンス = 共有リンク1件分
// 設定画面の3ステップ（リンク入力・送信先・アクセス制限）で入力した内容をまとめて保存する
@Entity
@Table(name = "shares")
public class Share {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 「この共有リンクは誰が作ったか」を表す紐（FK → users の id）
	// JWTから分かったユーザーをここに入れる。これで「自分のリンクだけ」を取り出せる
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	// ---- ステップ1：リンク入力 ----
	@Column(nullable = false, length = 2048) // URLは長くなることがあるので多めに取る
	private String url;

	@Column(nullable = false)
	private String title;

	// ---- ステップ2：送信先 ----
	@Column(nullable = false)
	private String recipientName;

	@Column(nullable = false)
	private String recipientEmail;

	@Column(nullable = false, length = 1000)
	private String message;

	// ---- ステップ3：アクセス制限 ----
	// ユーザーのパスワードと同じく、そのままではなくBCryptでハッシュ化して保存する
	@Column(nullable = false)
	private String passwordHash;

	// 有効期限（日付だけ。この日まで見られる）
	@Column(nullable = false)
	private LocalDate expiresAt;

	// 共有用URL（/s/xxxx）に使うランダムな文字列。id（1, 2, 3…）だと他人のリンクを推測されるので使わない
	@Column(nullable = false, unique = true)
	private String slug;

	@Column(nullable = false)
	private Instant createdAt;

	// JPAがDBから読み込むときに使う空のコンストラクタ（必須）
	protected Share() {
	}

	public Share(User user, String url, String title, String recipientName, String recipientEmail,
			String message, String passwordHash, LocalDate expiresAt, String slug) {
		this.user = user;
		this.url = url;
		this.title = title;
		this.recipientName = recipientName;
		this.recipientEmail = recipientEmail;
		this.message = message;
		this.passwordHash = passwordHash;
		this.expiresAt = expiresAt;
		this.slug = slug;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		return title;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public String getMessage() {
		return message;
	}

	public LocalDate getExpiresAt() {
		return expiresAt;
	}

	public String getSlug() {
		return slug;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
