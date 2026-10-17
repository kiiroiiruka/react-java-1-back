package com.example.backend.controller;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
//クラス　@RestController	「このクラスはAPI用。戻り値はJSONなどで返す」と宣言する
@RestController
public class HelloController {

	@GetMapping("/api/hello")
	public Map<String, String> hello() {
		return Map.of("message", "hello from Spring Boot");
	}
}
