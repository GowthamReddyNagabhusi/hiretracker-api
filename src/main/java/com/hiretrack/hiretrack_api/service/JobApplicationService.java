package com.hiretrack.hiretrack_api.service;

import com.hiretrack.hiretrack_api.dto.JobApplicationRequest;
import com.hiretrack.hiretrack_api.dto.StatsResponse;
import com.hiretrack.hiretrack_api.exception.ResourceNotFoundException;
import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.model.User;
import com.hiretrack.hiretrack_api.repository.JobApplicationRepository;
import com.hiretrack.hiretrack_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobRepo;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public JobApplicationService(JobApplicationRepository jobRepo,
                                  UserRepository userRepository,
                                  FileStorageService fileStorageService) {
        this.jobRepo = jobRepo;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public JobApplication create(JobApplicationRequest request) {
        User user = getCurrentUser();
        JobApplication job = new JobApplication();
        mapRequestToJob(request, job);
        job.setUser(user);
        return jobRepo.save(job);
    }

    public Page<JobApplication> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedDate").descending());
        return jobRepo.findByUser(getCurrentUser(), pageable);
    }

    public List<JobApplication> getAllList() {
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
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized — this is not your application");
        }

        mapRequestToJob(request, job);
        return jobRepo.save(job);
    }

    public JobApplication uploadResume(Long id, MultipartFile file) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        // Delete old file if exists
        if (job.getResumeFilePath() != null) {
            fileStorageService.deleteFile(job.getResumeFilePath());
        }

        String fileName = fileStorageService.storeFile(file);
        job.setResumeFileName(file.getOriginalFilename());
        job.setResumeFilePath(fileName);
        return jobRepo.save(job);
    }

    public void delete(Long id) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (!job.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized — this is not your application");
        }

        if (job.getResumeFilePath() != null) {
            fileStorageService.deleteFile(job.getResumeFilePath());
        }

        jobRepo.delete(job);
    }

    public StatsResponse getStats() {
        User user = getCurrentUser();
        long total       = jobRepo.findByUser(user).size();
        long interviews  = jobRepo.countByUserAndStatus(user, JobApplication.Status.INTERVIEW);
        long offers      = jobRepo.countByUserAndStatus(user, JobApplication.Status.OFFER);
        long rejections  = jobRepo.countByUserAndStatus(user, JobApplication.Status.REJECTED);
        double offerRate = total > 0 ? (double) offers / total * 100 : 0;
        return new StatsResponse(total, interviews, offers, rejections, offerRate);
    }

    private void mapRequestToJob(JobApplicationRequest request, JobApplication job) {
        job.setCompany(request.getCompany());
        job.setRole(request.getRole());
        job.setStatus(request.getStatus());
        job.setNotes(request.getNotes());
        job.setAppliedDate(request.getAppliedDate());
        if (request.getPriority() != null) job.setPriority(request.getPriority());
    }
}