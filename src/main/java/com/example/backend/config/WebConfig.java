// パッケージ名（フォルダ構成と対応）
package com.example.backend.config;
// configパッケージには、バックエンド全体に適用される設定を記載する
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// ※@ConfigurationがついているためSpringがWebConfigをnewしてDIコンテナに登録する。
// さらにWebMvcConfigurerを実装しているため、SpringはこのクラスをWeb設定用だと認識し、
// オーバーライドしたaddCorsMappingsを起動時に呼び出してCORS設定を反映する。

// CORS→「別のサイトから API を呼んでもいいか」をサーバーが許可する仕組み
@Configuration
public class WebConfig implements WebMvcConfigurer {

	// 許可するフロントのURL。application.properties の app.cors-allowed-origins から受け取る
	// （カンマ区切りで書くと、Springが配列に分けてくれる）
	private final String[] allowedOrigins;

	public WebConfig(@Value("${app.cors-allowed-origins}") String[] allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	//適応方法は二つある・
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry
			.addMapping("/api/**")// /api/ から始まるURLがフロントから来たらCORSを許可する
			.allowedOrigins(allowedOrigins)
			.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
	}
}
/*

上記は下記のコードと同じ役割を持っているらしい

@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE");
            }
        };
    }
}
 */