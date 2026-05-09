package com.hiretrack.hiretrack_api.dto;

import com.hiretrack.hiretrack_api.model.JobApplication;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import com.hiretrack.hiretrack_api.model.JobApplication;

public class JobApplicationRequest {
    @NotBlank(message = "Company is required")
    private String company;

    @NotBlank(message = "Role is required")
    private String role;

    private JobApplication.Status status = JobApplication.Status.APPLIED;

    private String notes;

    @NotNull(message = "Applied date is required")
    private LocalDate appliedDate;
    private JobApplication.Priority priority = JobApplication.Priority.MEDIUM;

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public JobApplication.Status getStatus() { return status; }
    public void setStatus(JobApplication.Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getAppliedDate() { return appliedDate; }
    public void setAppliedDate(LocalDate appliedDate) { this.appliedDate = appliedDate; }
    public JobApplication.Priority getPriority() { return priority; }
    public void setPriority(JobApplication.Priority priority) { this.priority = priority; }
}