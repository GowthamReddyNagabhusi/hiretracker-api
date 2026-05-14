package com.hiretrack.hiretrack_api.service;

import com.hiretrack.hiretrack_api.dto.JobApplicationRequest;
import com.hiretrack.hiretrack_api.dto.JobApplicationResponse;
import com.hiretrack.hiretrack_api.dto.StatsResponse;
import com.hiretrack.hiretrack_api.exception.AccessDeniedException;
import com.hiretrack.hiretrack_api.exception.ResourceNotFoundException;
import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.model.User;
import com.hiretrack.hiretrack_api.repository.JobApplicationRepository;
import com.hiretrack.hiretrack_api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobApplicationService {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationService.class);

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

    /**
     * Verifies that the given job application belongs to the current user.
     * Throws AccessDeniedException (403) if not.
     */
    private void verifyOwnership(JobApplication job, User user) {
        if (!job.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to access job application {} owned by user {}",
                    user.getId(), job.getId(), job.getUser().getId());
            throw new AccessDeniedException("You do not have permission to access this application");
        }
    }

    @Transactional
    public JobApplicationResponse create(JobApplicationRequest request) {
        User user = getCurrentUser();
        JobApplication job = new JobApplication();
        mapRequestToJob(request, job);
        job.setUser(user);
        JobApplication saved = jobRepo.save(job);
        log.info("User {} created job application {} for {}", user.getEmail(), saved.getId(), saved.getCompany());
        return new JobApplicationResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appliedDate").descending());
        return jobRepo.findByUser(getCurrentUser(), pageable)
                .map(JobApplicationResponse::new);
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getAllList() {
        return jobRepo.findByUser(getCurrentUser()).stream()
                .map(JobApplicationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> getByStatus(JobApplication.Status status) {
        return jobRepo.findByUserAndStatus(getCurrentUser(), status).stream()
                .map(JobApplicationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponse> searchByCompany(String company) {
        return jobRepo.findByUserAndCompanyContainingIgnoreCase(getCurrentUser(), company).stream()
                .map(JobApplicationResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public JobApplicationResponse update(Long id, JobApplicationRequest request) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        verifyOwnership(job, user);

        mapRequestToJob(request, job);
        JobApplication updated = jobRepo.save(job);
        log.info("User {} updated job application {}", user.getEmail(), id);
        return new JobApplicationResponse(updated);
    }

    @Transactional
    public JobApplicationResponse uploadResume(Long id, MultipartFile file) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        verifyOwnership(job, user);

        // Delete old file if exists
        if (job.getResumeFilePath() != null) {
            fileStorageService.deleteFile(job.getResumeFilePath());
        }

        String fileName = fileStorageService.storeFile(file);
        job.setResumeFileName(file.getOriginalFilename());
        job.setResumeFilePath(fileName);
        JobApplication updated = jobRepo.save(job);
        log.info("User {} uploaded resume for job application {}", user.getEmail(), id);
        return new JobApplicationResponse(updated);
    }

    @Transactional
    public void delete(Long id) {
        User user = getCurrentUser();
        JobApplication job = jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        verifyOwnership(job, user);

        if (job.getResumeFilePath() != null) {
            fileStorageService.deleteFile(job.getResumeFilePath());
        }

        jobRepo.delete(job);
        log.info("User {} deleted job application {}", user.getEmail(), id);
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats() {
        User user = getCurrentUser();
        // Use COUNT queries instead of loading all entities into memory
        long total       = jobRepo.countByUser(user);
        long interviews  = jobRepo.countByUserAndStatus(user, JobApplication.Status.INTERVIEW);
        long offers      = jobRepo.countByUserAndStatus(user, JobApplication.Status.OFFER);
        long rejections  = jobRepo.countByUserAndStatus(user, JobApplication.Status.REJECTED);
        double offerRate = total > 0 ? (double) offers / total * 100 : 0;
        return new StatsResponse(total, interviews, offers, rejections, offerRate);
    }

    private void mapRequestToJob(JobApplicationRequest request, JobApplication job) {
        job.setCompany(request.getCompany().trim());
        job.setRole(request.getRole().trim());
        job.setStatus(request.getStatus());
        job.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);
        job.setAppliedDate(request.getAppliedDate());
        if (request.getPriority() != null) {
            job.setPriority(request.getPriority());
        }
    }
}