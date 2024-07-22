package com.br.foliveira.test_pacto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.br.foliveira.test_pacto.models.Job;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>{
	List<Job> findByTitleContaining(String name);
}
