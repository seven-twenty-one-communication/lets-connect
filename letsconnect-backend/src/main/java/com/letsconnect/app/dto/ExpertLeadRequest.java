package com.letsconnect.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpertLeadRequest {

    @NotBlank(message = "Name is required")
    @Pattern(regexp = "^[a-zA-Z\\s]{3,50}$", message = "Name must contain letters and spaces only, 3 to 50 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Please enter a valid 10-digit Indian mobile number")
    private String phoneNumber;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Primary language is required")
    private String primaryLanguage;

    @NotBlank(message = "Preferred time slot is required")
    private String preferredSlot;

    private String notes;
}
