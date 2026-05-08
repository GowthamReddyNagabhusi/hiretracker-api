package com.hiretrack.hiretrack_api.controller;

import com.hiretrack.hiretrack_api.dto.JobApplicationRequest;
import com.hiretrack.hiretrack_api.dto.StatsResponse;
import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobApplicationService jobService;

    public JobController(JobApplicationService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobApplication> create(@Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.ok(jobService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<JobApplication>> getAll() {
        return ResponseEntity.ok(jobService.getAll());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<JobApplication>> filter(
            @RequestParam(required = false) JobApplication.Status status,
            @RequestParam(required = false) String company) {

        if (status != null) {
            return ResponseEntity.ok(jobService.getByStatus(status));
        }
        if (company != null) {
            return ResponseEntity.ok(jobService.searchByCompany(company));
        }
        return ResponseEntity.ok(jobService.getAll());
    }

    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(jobService.getStats());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplication> update(
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request) {
        return ResponseEntity.ok(jobService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        jobService.delete(id);
        return ResponseEntity.ok("Application deleted successfully");
    }
}