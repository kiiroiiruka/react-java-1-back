package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
//データの形を受け取る
import com.example.backend.entity.User;

// interfaceを書くだけで、Springが中身（SQL）を自動で作ってくれる
// 以下にDBとやり取りする処理を書く
public interface UserRepository extends JpaRepository<User, Long> {
	// メソッド名から「emailで検索するSQL」が自動で作られる
	//「SELECT * FROM users WHERE email = ?」の処理
	//「Optional<User>」は返すデータの返り値の型。
	//この型は「entity.User」で決まっている。
	Optional<User> findByEmail(String email);
	//「boolean」は返すデータの返り値の型。
	boolean existsByEmail(String email);
}
