package com.lec.packages.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.lec.packages.domain.exercise_code_table;

public interface ExerciseRepository extends JpaRepository<exercise_code_table, String> {
}

