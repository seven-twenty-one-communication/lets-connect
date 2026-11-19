package com.letsconnect.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallTokenResponse {
    private UUID callSessionId;
    private String channelName;
    private String agoraAppId;
    private String agoraToken;
    private Integer ratePerMinuteCoins;
    private Long callerBalance;
}
