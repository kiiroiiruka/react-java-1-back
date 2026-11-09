package com.example.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ShareRequest;
import com.example.backend.dto.ShareResponse;
import com.example.backend.service.ShareService;

import jakarta.validation.Valid;

// 共有リンクのAPI。SecurityConfig の設定で、ここは全部JWTが必要
@RestController
@RequestMapping("/api/shares")
public class ShareController {

	private final ShareService shareService;

	public ShareController(ShareService shareService) {
		this.shareService = shareService;
	}

	// POST /api/shares  共有リンクを作る（設定画面の「完了」ボタン）
	// 「誰が作ったか」はリクエストの中身ではなく、JWTの sub（ユーザーID）から決める
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ShareResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ShareRequest req) {
		return shareService.create(Long.valueOf(jwt.getSubject()), req);
	}

	// GET /api/shares  自分が作った共有リンクの一覧
	@GetMapping
	public List<ShareResponse> list(@AuthenticationPrincipal Jwt jwt) {
		return shareService.list(Long.valueOf(jwt.getSubject()));
	}
}
