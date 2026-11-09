package com.example.backend.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

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
// 設定画面の2ステップ（リンク入力・アクセス制限）で入力した内容をまとめて保存する
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

	// 公開ページの中に埋め込んで（iframeで）表示するときのURL
	// 埋め込みを禁止しているサイトは null（そのときは、パスワード入力後にサイトへ移動する）
	@Column(length = 2048)
	private String embedUrl;

	// ---- ステップ2：アクセス制限 ----
	// ユーザーのパスワードと同じく、そのままではなくBCryptでハッシュ化して保存する
	@Column(nullable = false)
	private String passwordHash;

	// 有効期限（日付だけ。この日まで見られる）
	@Column(nullable = false)
	private LocalDate expiresAt;

	// どの国の時刻で有効期限を判定するか（"Asia/Tokyo" などのIANAタイムゾーンID）
	// 同じ「12/31まで」でも、日本とニューヨークでは終わる瞬間が14時間ずれる
	@Column(nullable = false)
	private String timeZone;

	// 共有用URL（/s/xxxx）に使うランダムな文字列。id（1, 2, 3…）だと他人のリンクを推測されるので使わない
	@Column(nullable = false, unique = true)
	private String slug;

	@Column(nullable = false)
	private Instant createdAt;

	// JPAがDBから読み込むときに使う空のコンストラクタ（必須）
	protected Share() {
	}

	public Share(User user, String url, String embedUrl, String title, String passwordHash,
			LocalDate expiresAt, String timeZone, String slug) {
		this.user = user;
		this.url = url;
		this.embedUrl = embedUrl;
		this.title = title;
		this.passwordHash = passwordHash;
		this.expiresAt = expiresAt;
		this.timeZone = timeZone;
		this.slug = slug;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getUrl() {
		return url;
	}

	public String getEmbedUrl() {
		return embedUrl;
	}

	public boolean isEmbeddable() {
		return embedUrl != null;
	}

	public String getTitle() {
		return title;
	}

	public LocalDate getExpiresAt() {
		return expiresAt;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getTimeZone() {
		return timeZone;
	}

	// 有効期限の日を過ぎているか（期限の日の終わりまでは見られる）
	// 「今日」は、このリンクに設定されたタイムゾーンでの今日で判定する
	public boolean isExpired() {
		LocalDate today = LocalDate.now(ZoneId.of(timeZone));
		return today.isAfter(expiresAt);
	}

	// 閲覧できなくなる瞬間（有効期限の翌日の0:00。そのタイムゾーンで）
	// 公開ページで埋め込み表示しているとき、この時刻になったら画面から消すのに使う
	public Instant getClosesAt() {
		return expiresAt.plusDays(1).atStartOfDay(ZoneId.of(timeZone)).toInstant();
	}

	public String getSlug() {
		return slug;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
