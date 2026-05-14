package com.hiretrack.hiretrack_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hiretrack.hiretrack_api.dto.JobApplicationRequest;
import com.hiretrack.hiretrack_api.dto.JobApplicationResponse;
import com.hiretrack.hiretrack_api.dto.StatsResponse;
import com.hiretrack.hiretrack_api.model.JobApplication;
import com.hiretrack.hiretrack_api.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JobController API Tests")
class JobControllerTest {

    private MockMvc mockMvc;
    @Autowired private WebApplicationContext context;
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean private JobApplicationService jobService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    private JobApplicationResponse createSampleResponse() {
        JobApplicationResponse resp = new JobApplicationResponse();
        resp.setId(1L);
        resp.setCompany("Google");
        resp.setRole("Backend Engineer");
        resp.setStatus(JobApplication.Status.APPLIED);
        resp.setAppliedDate(LocalDate.of(2026, 5, 1));
        return resp;
    }

    @Test
    @DisplayName("GET /api/jobs — 403 without authentication")
    void getAll_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/jobs — 200 with authentication")
    void getAll_withAuth_returns200() throws Exception {
        when(jobService.getAllList()).thenReturn(List.of(createSampleResponse()));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].company").value("Google"))
                .andExpect(jsonPath("$[0].role").value("Backend Engineer"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/jobs — 201 on valid creation")
    void create_validRequest_returns201() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setCompany("Google");
        request.setRole("Backend Engineer");
        request.setAppliedDate(LocalDate.of(2026, 5, 1));

        when(jobService.create(any())).thenReturn(createSampleResponse());

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company").value("Google"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/jobs — 400 on missing company")
    void create_missingCompany_returns400() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setRole("Backend Engineer");
        request.setAppliedDate(LocalDate.of(2026, 5, 1));

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/jobs/{id} — 200 on valid update")
    void update_validRequest_returns200() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest();
        request.setCompany("Google");
        request.setRole("Senior Engineer");
        request.setStatus(JobApplication.Status.INTERVIEW);
        request.setAppliedDate(LocalDate.of(2026, 5, 1));

        when(jobService.update(eq(1L), any())).thenReturn(createSampleResponse());

        mockMvc.perform(put("/api/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/jobs/{id} — 204 on successful delete")
    void delete_returns204() throws Exception {
        doNothing().when(jobService).delete(1L);

        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isNoContent());

        verify(jobService).delete(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/jobs/stats — 200 with stats data")
    void getStats_returns200() throws Exception {
        StatsResponse stats = new StatsResponse(10, 3, 2, 4, 20.0);
        when(jobService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/jobs/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplied").value(10))
                .andExpect(jsonPath("$.totalOffers").value(2))
                .andExpect(jsonPath("$.offerRate").value(20.0));
    }
}
