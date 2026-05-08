package com.hiretrack.hiretrack_api.service;

import com.hiretrack.hiretrack_api.dto.JobApplicationRequest;
import com.hiretrack.hiretrack_api.dto.StatsResponse;
import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.model.User;
import com.hiretrack.hiretrack_api.repository.JobApplicationRepository;
import com.hiretrack.hiretrack_api.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobRepo;
    private final UserRepository userRepository;

    public JobApplicationService(JobApplicationRepository jobRepo,
                                  UserRepository userRepository) {
        this.jobRepo = jobRepo;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public JobApplication create(JobApplicationRequest request) {
        User user = getCurrentUser();
        JobApplication job = new JobApplication();
        job.setCompany(request.getCompany());
        job.setRole(request.getRole());
        job.setStatus(request.getStatus());
        job.setNotes(request.getNotes());
        job.setAppliedDate(request.getAppliedDate());
        job.setUser(user);
        return jobRepo.save(job);
    }

    public List<JobApplication> getAll() {
        return jobRepo.findByUser(getCurrentUser());
    }

    public List<JobApplication> getByStatus(JobApplication.Status status) {
        return jobRepo.findByUserAndStatus(getCurrentUser(), status);
    }

    public List<JobApplication> searchByCompany(String company) {
        return jobRepo.findByUserAndCompanyContainingIgnoreCase(getCurrentUser(), company);
    }

    public JobApplication update(Long id, JobApplicationRequest request) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        job.setCompany(request.getCompany());
        job.setRole(request.getRole());
        job.setStatus(request.getStatus());
        job.setNotes(request.getNotes());
        job.setAppliedDate(request.getAppliedDate());
        return jobRepo.save(job);
    }

    public void delete(Long id) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        jobRepo.delete(job);
    }

    public StatsResponse getStats() {
        User user = getCurrentUser();
        long total = jobRepo.findByUser(user).size();
        long interviews = jobRepo.countByUserAndStatus(user, JobApplication.Status.INTERVIEW);
        long offers = jobRepo.countByUserAndStatus(user, JobApplication.Status.OFFER);
        long rejections = jobRepo.countByUserAndStatus(user, JobApplication.Status.REJECTED);
        double offerRate = total > 0 ? (double) offers / total * 100 : 0;
        return new StatsResponse(total, interviews, offers, rejections, offerRate);
    }
}