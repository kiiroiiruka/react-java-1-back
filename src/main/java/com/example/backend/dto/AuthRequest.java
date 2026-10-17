package com.example.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

//フロントからデータを受け取った瞬間にデータの形をチェックする関数
// フロントから送られてくる { "email": "...", "password": "..." } に対応
// コントローラーで @Valid を付けると、下のチェックに通らない場合は自動で 400 を返す

//email と password の2つを持つデータの入れ物として宣言
//@NotBlankで受け取ったデータが空じゃないかをチェック、
//同時にメールアドレスの形じゃないかを@Emailでチェック
//@Sizeで文字数のチェックをしている
public record AuthRequest(
		@NotBlank @Email String email,
		@NotBlank @Size(min = 8, max = 72) String password) {
}
