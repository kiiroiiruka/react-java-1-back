package com.example.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.OpenShareRequest;
import com.example.backend.dto.OpenShareResponse;
import com.example.backend.dto.PublicShareResponse;
import com.example.backend.service.ShareService;

import jakarta.validation.Valid;

// 共有リンクを受け取った人が使うAPI（公開ページ /s/{slug} から呼ばれる）
// 受け取った人はこのアプリのユーザーではないので、SecurityConfig で /api/public/** はJWTなしでOKにしている
// その代わり、リンクのパスワードと有効期限でチェックする
@RestController
@RequestMapping("/api/public/shares")
public class PublicShareController {

	private final ShareService shareService;

	public PublicShareController(ShareService shareService) {
		this.shareService = shareService;
	}

	// GET /api/public/shares/{slug}  リンクが有効か（存在する・期限内）を確認する
	// @PathVariable URLの {slug} の部分を受け取る
	@GetMapping("/{slug}")
	public PublicShareResponse get(@PathVariable String slug) {
		return shareService.getPublic(slug);
	}

	// POST /api/public/shares/{slug}/open  パスワードが合っていれば、共有されたURLを返す
	@PostMapping("/{slug}/open")
	public OpenShareResponse open(@PathVariable String slug, @Valid @RequestBody OpenShareRequest req) {
		return shareService.open(slug, req.password());
	}
}
