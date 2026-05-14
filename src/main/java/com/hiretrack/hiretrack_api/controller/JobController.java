package com.hiretrack.hiretrack_api.controller;

import com.hiretrack.hiretrack_api.dto.JobApplicationRequest;
import com.hiretrack.hiretrack_api.dto.JobApplicationResponse;
import com.hiretrack.hiretrack_api.dto.StatsResponse;
import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobApplicationService jobService;

    public JobController(JobApplicationService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(@Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.create(request));
    }

    // Paginated endpoint — /api/jobs/paged?page=0&size=10
    @GetMapping("/paged")
    public ResponseEntity<Page<JobApplicationResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getAll(page, size));
    }

    // Non-paginated — for frontend to load all at once
    @GetMapping
    public ResponseEntity<List<JobApplicationResponse>> getAll() {
        return ResponseEntity.ok(jobService.getAllList());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<JobApplicationResponse>> filter(
            @RequestParam(required = false) JobApplication.Status status,
            @RequestParam(required = false) String company) {
        if (status != null) return ResponseEntity.ok(jobService.getByStatus(status));
        if (company != null) return ResponseEntity.ok(jobService.searchByCompany(company));
        return ResponseEntity.ok(jobService.getAllList());
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(jobService.getStats());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.ok(jobService.update(id, request));
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<JobApplicationResponse> uploadResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(jobService.uploadResume(id, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.noContent().build();
    }
}