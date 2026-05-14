package com.hiretrack.hiretrack_api.dto;

import com.hiretrack.hiretrack_api.model.JobApplication;

import java.time.LocalDate;

/**
 * Response DTO for JobApplication — decouples API contract from JPA entity.
 * Never exposes internal fields like User object or file system paths.
 */
public class JobApplicationResponse {

    private Long id;
    private String company;
    private String role;
    private JobApplication.Status status;
    private JobApplication.Priority priority;
    private String notes;
    private LocalDate appliedDate;
    private String resumeFileName;

    // Constructor for mapping from entity
    public JobApplicationResponse(JobApplication entity) {
        this.id = entity.getId();
        this.company = entity.getCompany();
        this.role = entity.getRole();
        this.status = entity.getStatus();
        this.priority = entity.getPriority();
        this.notes = entity.getNotes();
        this.appliedDate = entity.getAppliedDate();
        this.resumeFileName = entity.getResumeFileName();
        // Intentionally NOT exposing: user, resumeFilePath (internal path)
    }

    // Default constructor for Jackson
    public JobApplicationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public JobApplication.Status getStatus() { return status; }
    public void setStatus(JobApplication.Status status) { this.status = status; }

    public JobApplication.Priority getPriority() { return priority; }
    public void setPriority(JobApplication.Priority priority) { this.priority = priority; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }

    public String getResumeFileName() { return resumeFileName; }
    public void setResumeFileName(String resumeFileName) { this.resumeFileName = resumeFileName; }
}
