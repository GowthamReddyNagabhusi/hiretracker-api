package com.hiretrack.hiretrack_api.repository;

import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUser(User user);
    List<JobApplication> findByUserAndStatus(User user, JobApplication.Status status);
    List<JobApplication> findByUserAndCompanyContainingIgnoreCase(User user, String company);
    long countByUserAndStatus(User user, JobApplication.Status status);
}