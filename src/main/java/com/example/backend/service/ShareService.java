package com.example.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.dto.ShareRequest;
import com.example.backend.dto.ShareResponse;
import com.example.backend.entity.Share;
import com.example.backend.entity.User;
import com.example.backend.repository.ShareRepository;
import com.example.backend.repository.UserRepository;

// 共有リンクの作成・一覧を担当する
@Service
public class ShareService {

	// BCryptが扱えるパスワードの最大バイト数（日本語は1文字3バイト）
	private static final int MAX_PASSWORD_BYTES = 72;

	// 推測できないランダムな値を作るための道具
	private static final SecureRandom RANDOM = new SecureRandom();

	private final ShareRepository shareRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public ShareService(ShareRepository shareRepository, UserRepository userRepository,
			PasswordEncoder passwordEncoder) {
		this.shareRepository = shareRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// userId は JWT から取り出したもの（= ログイン中の本人）
	// フロントから送られてきた値ではないので、他人になりすまして保存することはできない
	@Transactional
	public ShareResponse create(Long userId, ShareRequest req) {
		if (req.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "パスワードが長すぎます");
		}
		User user = findUser(userId);
		Share share = new Share(
				user,
				req.url().trim(),
				req.title().trim(),
				req.recipientName().trim(),
				req.recipientEmail().trim(),
				req.message(),
				// 共有リンクのパスワードも、ユーザーのパスワードと同じくハッシュ化してから保存する
				passwordEncoder.encode(req.password()),
				req.expiresAt(),
				newSlug());
		return ShareResponse.from(shareRepository.save(share));
	}

	// ログイン中の本人が作った共有リンクだけを、新しい順に返す
	@Transactional(readOnly = true)
	public List<ShareResponse> list(Long userId) {
		return shareRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
				.map(ShareResponse::from)
				.toList();
	}

	// JWTは本物でも、その後ユーザーが削除されていることがあるので、そのときは401
	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
	}

	// 9バイトのランダムな値を、URLに入れても大丈夫な12文字にする（例：k3Jx9qLm_aB2）
	private String newSlug() {
		byte[] bytes = new byte[9];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
}
