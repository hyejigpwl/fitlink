package com.lec.packages.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.lec.packages.domain.Member;

public interface MemberRepository extends JpaRepository<Member, String>{

	@EntityGraph(attributePaths = {"roleSet", "memExercise", "memClub"})
	@Query("SELECT m FROM Member m " +
		       "LEFT JOIN FETCH m.memExercise e " +
		       "LEFT JOIN FETCH m.memClub c " +
		       "WHERE m.memId = :memId")
	Optional<Member> getWithRoles(@Param("memId") String mid);

	@EntityGraph(attributePaths = {"roleSet", "memExercise", "memClub"})
	@Query("SELECT m FROM Member m " +
		       "LEFT JOIN FETCH m.memExercise e " +
		       "LEFT JOIN FETCH m.memClub c " +
		       "WHERE m.memEmail = :memEmail and m.memSocial = true")
	Optional<Member> findByMemEmail(@Param("memEmail") String memEmail);

	Optional<Member> findByMemIdAndMemName(String memId, String memName);
	
}
