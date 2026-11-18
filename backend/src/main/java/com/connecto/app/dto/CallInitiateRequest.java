package com.connecto.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallInitiateRequest {
    @NotNull
    private UUID callerId;
    @NotNull
    private UUID listenerId;
    private Integer categoryId;
}
