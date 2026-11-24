package com.letsconnect.app.controller;

import com.letsconnect.app.dto.ExpertLeadRequest;
import com.letsconnect.app.dto.ExpertLeadResponse;
import com.letsconnect.app.model.entity.ExpertLead;
import com.letsconnect.app.repository.ExpertLeadRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user/web/expert-leads")
@RequiredArgsConstructor
@Slf4j
public class WebLeadController {

    private final ExpertLeadRepository expertLeadRepository;

    @PostMapping
    public ResponseEntity<?> submitLead(@Valid @RequestBody ExpertLeadRequest request) {
        log.info("Received expert lead submission for phone: {}", request.getPhoneNumber());

        ExpertLead lead = ExpertLead.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .gender(request.getGender())
                .primaryLanguage(request.getPrimaryLanguage())
                .preferredSlot(request.getPreferredSlot())
                .notes(request.getNotes())
                .status("PENDING_REVIEW")
                .build();

        ExpertLead saved = expertLeadRepository.save(lead);
        log.info("Successfully recorded expert lead with id: {}", saved.getId());

        ExpertLeadResponse responseDto = ExpertLeadResponse.builder()
                .id(saved.getId())
                .fullName(saved.getFullName())
                .phoneNumber(saved.getPhoneNumber())
                .gender(saved.getGender())
                .primaryLanguage(saved.getPrimaryLanguage())
                .preferredSlot(saved.getPreferredSlot())
                .status(saved.getStatus())
                .notes(saved.getNotes())
                .createdAt(saved.getCreatedAt())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Your host application has been submitted successfully! Our onboarding team will contact you on WhatsApp.",
                "lead", responseDto
        ));
    }
}
