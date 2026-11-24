package com.letsconnect.app.dto;

import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertLeadResponse {

    private UUID id;
    private String fullName;
    private String phoneNumber;
    private String gender;
    private String primaryLanguage;
    private String preferredSlot;
    private String status;
    private String notes;
    private ZonedDateTime createdAt;
}
