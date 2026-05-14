package com.hiretrack.hiretrack_api.repository;

import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUser(User user);
    Page<JobApplication> findByUser(User user, Pageable pageable);
    List<JobApplication> findByUserAndStatus(User user, JobApplication.Status status);
    List<JobApplication> findByUserAndCompanyContainingIgnoreCase(User user, String company);
    long countByUser(User user);
    long countByUserAndStatus(User user, JobApplication.Status status);
}