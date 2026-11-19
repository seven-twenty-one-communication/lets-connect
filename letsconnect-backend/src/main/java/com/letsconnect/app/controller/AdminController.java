package com.letsconnect.app.controller;

import com.letsconnect.app.model.entity.ListenerProfile;
import com.letsconnect.app.model.entity.UserReport;
import com.letsconnect.app.model.enums.KycStatus;
import com.letsconnect.app.model.enums.UserRole;
import com.letsconnect.app.repository.ListenerProfileRepository;
import com.letsconnect.app.repository.UserReportRepository;
import com.letsconnect.app.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final ListenerProfileRepository listenerProfileRepository;
    private final UserRepository userRepository;
    private final UserReportRepository userReportRepository;

    @PatchMapping("/listeners/{userId}/kyc")
    @Transactional
    public ResponseEntity<?> updateListenerKyc(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateKycRequest request
    ) {
        ListenerProfile profile = listenerProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }
        if (profile.getKycStatus() != KycStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only pending applications can be reviewed"));
        }
        profile.setKycStatus(request.getStatus());
        if (request.getStatus() == KycStatus.REJECTED) {
            profile.setIsOnline(false);
            profile.getUser().setRole(UserRole.USER);
            userRepository.save(profile.getUser());
        }
        listenerProfileRepository.save(profile);
        return ResponseEntity.ok(Map.of("userId", userId, "kycStatus", profile.getKycStatus()));
    }

    @GetMapping("/reports")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Map<String, Object>>> getReports(
            @RequestParam(defaultValue = "OPEN") String status
    ) {
        if (!List.of("OPEN", "REVIEWED", "ACTIONED").contains(status)) {
            return ResponseEntity.badRequest().build();
        }
        List<Map<String, Object>> reports = userReportRepository.findAll().stream()
                .filter(report -> status.equals(report.getStatus()))
                .map(this::reportResponse)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @PatchMapping("/reports/{reportId}")
    @Transactional
    public ResponseEntity<?> updateReport(
            @PathVariable UUID reportId,
            @Valid @RequestBody UpdateReportRequest request
    ) {
        UserReport report = userReportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        report.setStatus(request.getStatus().name());
        userReportRepository.save(report);
        return ResponseEntity.ok(Map.of("reportId", reportId, "status", report.getStatus()));
    }

    private Map<String, Object> reportResponse(UserReport report) {
        return Map.of(
                "reportId", report.getId(),
                "reporterId", report.getReporter().getId(),
                "reportedUserId", report.getReportedUser().getId(),
                "reason", report.getReason(),
                "details", report.getDetails() == null ? "" : report.getDetails(),
                "status", report.getStatus(),
                "createdAt", report.getCreatedAt()
        );
    }

    @Data
    public static class UpdateKycRequest {
        @NotNull
        private KycStatus status;
    }

    @Data
    public static class UpdateReportRequest {
        @NotNull
        private ReportStatus status;
    }

    public enum ReportStatus {
        REVIEWED,
        ACTIONED
    }
}
