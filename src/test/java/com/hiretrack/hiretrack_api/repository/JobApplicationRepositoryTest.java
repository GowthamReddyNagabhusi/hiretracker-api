package com.hiretrack.hiretrack_api.repository;

import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("JobApplicationRepository Integration Tests")
class JobApplicationRepositoryTest {

    @Autowired private JobApplicationRepository jobRepo;
    @Autowired private UserRepository userRepo;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        jobRepo.deleteAll();
        userRepo.deleteAll();

        user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@test.com");
        user1.setPassword("encoded");
        user1 = userRepo.save(user1);

        user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@test.com");
        user2.setPassword("encoded");
        user2 = userRepo.save(user2);

        // Create jobs for user1
        createJob("Google", "Backend Engineer", JobApplication.Status.APPLIED, user1);
        createJob("Meta", "Frontend Engineer", JobApplication.Status.INTERVIEW, user1);
        createJob("Amazon", "SDE II", JobApplication.Status.OFFER, user1);
        createJob("Netflix", "Platform Engineer", JobApplication.Status.REJECTED, user1);

        // Create jobs for user2
        createJob("Apple", "iOS Engineer", JobApplication.Status.APPLIED, user2);
    }

    private void createJob(String company, String role, JobApplication.Status status, User user) {
        JobApplication job = new JobApplication();
        job.setCompany(company);
        job.setRole(role);
        job.setStatus(status);
        job.setAppliedDate(LocalDate.now());
        job.setUser(user);
        jobRepo.save(job);
    }

    @Test
    @DisplayName("findByUser returns only that user's jobs")
    void findByUser_returnsOnlyOwnJobs() {
        List<JobApplication> user1Jobs = jobRepo.findByUser(user1);
        List<JobApplication> user2Jobs = jobRepo.findByUser(user2);

        assertThat(user1Jobs).hasSize(4);
        assertThat(user2Jobs).hasSize(1);
        assertThat(user2Jobs.get(0).getCompany()).isEqualTo("Apple");
    }

    @Test
    @DisplayName("findByUser with pagination works correctly")
    void findByUser_paginated() {
        Page<JobApplication> page = jobRepo.findByUser(user1, PageRequest.of(0, 2));

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByUserAndStatus filters correctly")
    void findByUserAndStatus_filtersCorrectly() {
        List<JobApplication> interviews = jobRepo.findByUserAndStatus(user1, JobApplication.Status.INTERVIEW);

        assertThat(interviews).hasSize(1);
        assertThat(interviews.get(0).getCompany()).isEqualTo("Meta");
    }

    @Test
    @DisplayName("findByUserAndCompanyContainingIgnoreCase searches correctly")
    void searchByCompany_caseInsensitive() {
        List<JobApplication> results = jobRepo.findByUserAndCompanyContainingIgnoreCase(user1, "goo");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCompany()).isEqualTo("Google");
    }

    @Test
    @DisplayName("countByUser returns correct total")
    void countByUser_returnsCorrectCount() {
        long count = jobRepo.countByUser(user1);

        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("countByUserAndStatus returns correct filtered count")
    void countByUserAndStatus_returnsCorrectCount() {
        long rejections = jobRepo.countByUserAndStatus(user1, JobApplication.Status.REJECTED);

        assertThat(rejections).isEqualTo(1);
    }

    @Test
    @DisplayName("user isolation — user2 cannot see user1's data")
    void userIsolation_enforced() {
        List<JobApplication> user2Jobs = jobRepo.findByUserAndStatus(user2, JobApplication.Status.APPLIED);

        assertThat(user2Jobs).hasSize(1);
        assertThat(user2Jobs.get(0).getCompany()).isEqualTo("Apple");
        // Verify no cross-user leakage
        assertThat(user2Jobs).noneMatch(j -> j.getCompany().equals("Google"));
    }
}
