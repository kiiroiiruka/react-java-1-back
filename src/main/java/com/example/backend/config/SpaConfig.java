package com.example.backend.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

// 本番では、ビルドしたReact（index.html と assets/）を src/main/resources/static に入れて
// Spring Bootから一緒に配信する（Dockerfileでコピーしている）。
//
// React Routerの画面（/register や /verify など）はReactの中だけにあるURLなので、
// サーバーにはそんなファイルはない。そのまま開くと404になってしまうため、
// 「ファイルが見つからなければ index.html を返す」ようにして、あとはReactに画面を選ばせる。
//
// ※ /api/... はControllerが先に処理するので、ここには来ない
@Configuration
public class SpaConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry
			.addResourceHandler("/**")
			.addResourceLocations("classpath:/static/")
			.resourceChain(true)
			.addResolver(new PathResourceResolver() {
				@Override
				protected Resource getResource(String resourcePath, Resource location) throws IOException {
					// assets/index-xxxx.js のような本物のファイルがあれば、それを返す
					Resource requested = location.createRelative(resourcePath);
					if (!resourcePath.isEmpty() && requested.exists() && requested.isReadable()) {
						return requested;
					}
					// 存在しないAPIのURLにまで index.html を返さないようにする
					if (resourcePath.startsWith("api/")) {
						return null;
					}
					// それ以外（/register など）は index.html を返す
					// ローカル開発中は static にReactが入っていないので、そのときは何も返さない
					Resource index = new ClassPathResource("/static/index.html");
					return index.exists() ? index : null;
				}
			});
	}
}
