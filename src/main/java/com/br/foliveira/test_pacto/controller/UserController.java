package com.br.foliveira.test_pacto.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.foliveira.test_pacto.models.Job;
import com.br.foliveira.test_pacto.models.User;
import com.br.foliveira.test_pacto.repository.JobRepository;
import com.br.foliveira.test_pacto.repository.UserRepository;


@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<List<User>> getAllUsers() {
        var usersData = new ArrayList<User>();
		userRepository.findAll().forEach(usersData::add);
		
		return usersData.isEmpty() ? 
		new ResponseEntity<>(HttpStatus.NO_CONTENT)
		: new ResponseEntity<>(usersData, HttpStatus.OK);
	}
    
    @GetMapping("users/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ResponseEntity<User> getUserById(@PathVariable("userId") long id) {
        return userRepository.findById(id)
        .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
        .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("users/{userId}/avaliablejobs")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ResponseEntity<List<Job>> getAvaliableJobs(@PathVariable(value = "userId") long userId) {
        List<Job> jobsData = jobRepository.findAll();
        Set<Job> jobsApplied = userRepository.findById(userId).get().getJobs();
        return ResponseEntity.ok(
            jobsData.stream()
                .filter(job -> !jobsApplied.contains(job))
                .collect(Collectors.toList())
        );
    }
    
    @GetMapping("users/{userId}/myjobs")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ResponseEntity<List<Job>> getAppliedJobs(@PathVariable(value = "userId") long userId) {       
        return ResponseEntity.ok(
        	userRepository.findById(userId).get().getJobs()
        	.stream().collect(Collectors.toList())
        );
    }
    
    @PostMapping("users/{userId}/{jobId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	ResponseEntity<User> assignJob(@PathVariable(value = "userId") Long userId, 
        @PathVariable(value = "jobId") Long jobId) throws Exception {
		User userData = userRepository.findById(userId).get();
        Job jobData = jobRepository.findById(jobId).get();
		userRepository.findById(userId).map(user -> {
			Set<Job> jobs = userData.getJobs();
			if (!jobs.contains(jobData)) {
				user.addJob(jobData);
				jobRepository.save(jobData);
			}
			return userData;
		}).orElseThrow(() -> new Exception());
		return new ResponseEntity<>(userData, HttpStatus.CREATED);
	}
	
	@DeleteMapping("users/{userId}/{jobId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
	ResponseEntity<User> dismissJob(@PathVariable(value = "userId") Long userId, 
        @PathVariable(value = "jobId") Long jobId) throws Exception {
		User user = userRepository.findById(userId)
            .orElseThrow(() -> new Exception());
        user.removeJob(jobId);
        userRepository.save(user);
        
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
        
    @DeleteMapping("users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
	ResponseEntity<HttpStatus> deleteUser(@PathVariable("userId") long id) {		
        try {
	        userRepository.deleteById(id);
	        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
}
