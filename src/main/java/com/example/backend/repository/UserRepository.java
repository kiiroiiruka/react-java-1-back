package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
//データの形を受け取る
import com.example.backend.entity.User;

// interfaceを書くだけで、Springが中身（SQL）を自動で作ってくれる
// 以下にDBとやり取りする処理を書く
public interface UserRepository extends JpaRepository<User, Long> {

	// メソッド名から「emailで検索するSQL」が自動で作られる
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);
}
