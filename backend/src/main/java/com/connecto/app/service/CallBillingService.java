package com.connecto.app.service;

import com.connecto.app.model.enums.CallStatus;
import com.connecto.app.repository.CallSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallBillingService {
    private final CallSessionRepository callSessionRepository;
    private final CallBillingProcessor billingProcessor;

    @Scheduled(fixedRate = 60000)
    public void processActiveCallsBilling() {
        for (var call : callSessionRepository.findByStatus(CallStatus.CONNECTED)) {
            try {
                billingProcessor.processCall(call.getId());
            } catch (Exception e) {
                log.error("Failed to bill call session: {}", call.getId(), e);
            }
        }
    }
}
