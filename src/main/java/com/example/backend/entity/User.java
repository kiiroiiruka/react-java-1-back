package com.example.backend.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//@Entity このクラスはDBのテーブルに対応する。1つのインスタンス = テーブルの1行
@Entity
@Table(name = "users") // "user" はDBの予約語なので users にする
public class User {

	@Id // 主キー
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 1, 2, 3... とDBが自動で番号を振る
	private Long id;

	@Column(nullable = false, unique = true) // 必須・重複禁止
	private String email;

	// 平文ではなくBCryptでハッシュ化した値を入れる
	@Column(nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private Instant createdAt;

	// JPAがDBから読み込むときに使う空のコンストラクタ（必須）
	protected User() {
	}

	public User(String email, String passwordHash) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
