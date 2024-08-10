package com.br.foliveira.test_pacto.controller;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.foliveira.test_pacto.models.Job;
import com.br.foliveira.test_pacto.repository.JobRepository;

@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/")
    ResponseEntity<List<Job>> getAllJobs(@RequestParam(required = false) String title) {
	    List<Job> jobs = new ArrayList<>();
		if(title == null){
			jobRepository.findAll().forEach(jobs::add);
		}else{
			jobRepository.findByTitleContaining(title).forEach(jobs::add);
		}

		return jobs.isEmpty() ? 
			new ResponseEntity<>(HttpStatus.NO_CONTENT)
			: new ResponseEntity<>(jobs, HttpStatus.OK);
	}
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	public ResponseEntity<Job> getJobById(@PathVariable("id") long id) {
		return jobRepository.findById(id)
			.map(job -> new ResponseEntity<>(job, HttpStatus.OK))
			.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Job> postJob(@RequestBody Job jobRequest) {	
		try {
			Job jobData = jobRepository.save(new Job(jobRequest.getTitle(), jobRequest.getDescription()));
			return new ResponseEntity<>(jobData, HttpStatus.CREATED);
	    } catch (Exception e) {
	        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<HttpStatus> deleteJobById(@PathVariable("id") long id) {
	    try {
	        jobRepository.deleteById(id);
	        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
	    }
	}
}
