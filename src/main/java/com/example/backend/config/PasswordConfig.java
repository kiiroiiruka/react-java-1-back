//このファイルは com/example/backend/config フォルダにありますと宣言
package com.example.backend.config;
//@Beanのアノテーションを扱えるようにするクラス
import org.springframework.context.annotation.Bean;
//@Configurationのアノテーションを扱えるようにするクラス
import org.springframework.context.annotation.Configuration;
//パスワードをハッシュ化するためのクラス(BCryptPasswordEncoder())
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// PasswordEncoder 型（encode / matches を持つインターフェース）を使えるように読み込む
import org.springframework.security.crypto.password.PasswordEncoder;
//このクラスを設定クラスとして扱うと宣言する
@Configuration
public class PasswordConfig {
	/* 
	PasswordEncoder 型にするとメソッドが以下のような「encode / matches」
	の二つのメソッドの身が備わったクラスの型で宣言を行うという形になる。

	///public class BCryptPasswordEncoder implements PasswordEncoder {
	//                                  └ PasswordEncoder のルールに従います

		public String encode(CharSequence rawPassword) {
			// BCrypt でハッシュ化する処理（Spring が書いてくれている）
		}

		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			// BCrypt で一致するか確認する処理（Spring が書いてくれている）
		}
		}
	*/
	
	@Bean//←こいつを付けることでSpringが起動時に自動でpasswordEncoderを呼び出して
	//returnの内容をグローバルにファイルまたいで呼び出せるようにする。
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();//
	}
}
