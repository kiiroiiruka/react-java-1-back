package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.entity.Share;

// UserRepository と同じく、interfaceを書くだけでSpringが中身（SQL）を作ってくれる
public interface ShareRepository extends JpaRepository<Share, Long> {

	// SELECT * FROM shares WHERE user_id = ? ORDER BY created_at DESC が自動で作られる
	// （メソッド名の UserId は「user の id」と読まれる）
	List<Share> findByUserIdOrderByCreatedAtDesc(Long userId);
}
