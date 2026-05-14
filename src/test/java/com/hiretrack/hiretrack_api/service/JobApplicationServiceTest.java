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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobApplicationService Unit Tests")
class JobApplicationServiceTest {

    @Mock private JobApplicationRepository jobRepo;
    @Mock private UserRepository userRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private JobApplicationService jobService;

    private User currentUser;
    private User otherUser;
    private JobApplication testJob;
    private JobApplicationRequest jobRequest;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setName("Test User");
        currentUser.setEmail("test@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");

        testJob = new JobApplication();
        testJob.setId(1L);
        testJob.setCompany("Google");
        testJob.setRole("Backend Engineer");
        testJob.setStatus(JobApplication.Status.APPLIED);
        testJob.setAppliedDate(LocalDate.of(2026, 5, 1));
        testJob.setUser(currentUser);

        jobRequest = new JobApplicationRequest();
        jobRequest.setCompany("Google");
        jobRequest.setRole("Backend Engineer");
        jobRequest.setStatus(JobApplication.Status.APPLIED);
        jobRequest.setAppliedDate(LocalDate.of(2026, 5, 1));

        // Mock SecurityContext for getCurrentUser()
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void mockCurrentUser() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(currentUser));
    }

    @Nested
    @DisplayName("Create")
    class CreateTests {

        @Test
        @DisplayName("should create job application and return DTO")
        void create_success() {
            mockCurrentUser();
            when(jobRepo.save(any(JobApplication.class))).thenReturn(testJob);

            JobApplicationResponse response = jobService.create(jobRequest);

            assertThat(response).isNotNull();
            assertThat(response.getCompany()).isEqualTo("Google");
            assertThat(response.getRole()).isEqualTo("Backend Engineer");
            assertThat(response.getStatus()).isEqualTo(JobApplication.Status.APPLIED);

            verify(jobRepo).save(any(JobApplication.class));
        }
    }

    @Nested
    @DisplayName("Update")
    class UpdateTests {

        @Test
        @DisplayName("should update own job application")
        void update_ownJob_success() {
            mockCurrentUser();
            when(jobRepo.findById(1L)).thenReturn(Optional.of(testJob));
            when(jobRepo.save(any(JobApplication.class))).thenReturn(testJob);

            jobRequest.setStatus(JobApplication.Status.INTERVIEW);
            JobApplicationResponse response = jobService.update(1L, jobRequest);

            assertThat(response).isNotNull();
            verify(jobRepo).save(any(JobApplication.class));
        }

        @Test
        @DisplayName("should throw AccessDeniedException for other user's job")
        void update_otherUsersJob_throwsAccessDenied() {
            mockCurrentUser();
            testJob.setUser(otherUser); // Belongs to different user
            when(jobRepo.findById(1L)).thenReturn(Optional.of(testJob));

            assertThatThrownBy(() -> jobService.update(1L, jobRequest))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("permission");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for missing job")
        void update_nonExistent_throwsNotFound() {
            mockCurrentUser();
            when(jobRepo.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobService.update(999L, jobRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete")
    class DeleteTests {

        @Test
        @DisplayName("should delete own job application")
        void delete_ownJob_success() {
            mockCurrentUser();
            when(jobRepo.findById(1L)).thenReturn(Optional.of(testJob));

            jobService.delete(1L);

            verify(jobRepo).delete(testJob);
        }

        @Test
        @DisplayName("should throw AccessDeniedException for other user's job")
        void delete_otherUsersJob_throwsAccessDenied() {
            mockCurrentUser();
            testJob.setUser(otherUser);
            when(jobRepo.findById(1L)).thenReturn(Optional.of(testJob));

            assertThatThrownBy(() -> jobService.delete(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("should delete resume file when deleting job with resume")
        void delete_withResume_deletesFile() {
            mockCurrentUser();
            testJob.setResumeFilePath("uuid-resume.pdf");
            when(jobRepo.findById(1L)).thenReturn(Optional.of(testJob));

            jobService.delete(1L);

            verify(fileStorageService).deleteFile("uuid-resume.pdf");
            verify(jobRepo).delete(testJob);
        }
    }

    @Nested
    @DisplayName("Stats")
    class StatsTests {

        @Test
        @DisplayName("should return correct statistics")
        void getStats_returnsCorrectStats() {
            mockCurrentUser();
            when(jobRepo.countByUser(currentUser)).thenReturn(10L);
            when(jobRepo.countByUserAndStatus(currentUser, JobApplication.Status.INTERVIEW)).thenReturn(3L);
            when(jobRepo.countByUserAndStatus(currentUser, JobApplication.Status.OFFER)).thenReturn(2L);
            when(jobRepo.countByUserAndStatus(currentUser, JobApplication.Status.REJECTED)).thenReturn(4L);

            StatsResponse stats = jobService.getStats();

            assertThat(stats.getTotalApplied()).isEqualTo(10);
            assertThat(stats.getTotalInterviews()).isEqualTo(3);
            assertThat(stats.getTotalOffers()).isEqualTo(2);
            assertThat(stats.getTotalRejections()).isEqualTo(4);
            assertThat(stats.getOfferRate()).isEqualTo(20.0);
        }

        @Test
        @DisplayName("should handle zero applications without division error")
        void getStats_zeroApplications_noDivisionError() {
            mockCurrentUser();
            when(jobRepo.countByUser(currentUser)).thenReturn(0L);
            when(jobRepo.countByUserAndStatus(any(), any())).thenReturn(0L);

            StatsResponse stats = jobService.getStats();

            assertThat(stats.getTotalApplied()).isEqualTo(0);
            assertThat(stats.getOfferRate()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("List & Filter")
    class ListTests {

        @Test
        @DisplayName("should return all jobs as DTOs")
        void getAllList_returnsDTOs() {
            mockCurrentUser();
            when(jobRepo.findByUser(currentUser)).thenReturn(List.of(testJob));

            List<JobApplicationResponse> results = jobService.getAllList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getCompany()).isEqualTo("Google");
        }

        @Test
        @DisplayName("should filter by status")
        void getByStatus_filtersCorrectly() {
            mockCurrentUser();
            when(jobRepo.findByUserAndStatus(currentUser, JobApplication.Status.APPLIED))
                    .thenReturn(List.of(testJob));

            List<JobApplicationResponse> results = jobService.getByStatus(JobApplication.Status.APPLIED);

            assertThat(results).hasSize(1);
        }
    }
}
