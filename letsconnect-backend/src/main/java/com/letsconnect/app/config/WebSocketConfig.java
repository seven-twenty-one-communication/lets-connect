package com.letsconnect.app.config;

import com.letsconnect.app.repository.CallSessionRepository;
import com.letsconnect.app.repository.VoiceRoomParticipantRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final CallSessionRepository callSessionRepository;
    private final VoiceRoomParticipantRepository roomParticipantRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-calling")
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    throw new AccessDeniedException("Invalid WebSocket frame");
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Principal principal = accessor.getUser();
                    if (principal == null) {
                        principal = authenticate(accessor.getFirstNativeHeader("Authorization"));
                        accessor.setUser(principal);
                    }
                    if (principal == null) {
                        throw new AccessDeniedException("A valid bearer token is required");
                    }
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeSubscription(accessor.getUser(), accessor.getDestination());
                } else if (StompCommand.SEND.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination == null || !destination.startsWith("/app/")) {
                        throw new AccessDeniedException("Client messages must use an application destination");
                    }
                }
                return message;
            }
        });
    }

    private Principal authenticate(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload();
            String subject = claims.getSubject();
            String role = claims.get("role", String.class);
            if (subject == null || role == null) {
                return null;
            }
            return new UsernamePasswordAuthenticationToken(
                    subject,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    private void authorizeSubscription(Principal principal, String destination) {
        if (principal == null || destination == null) {
            throw new AccessDeniedException("Authenticated subscriptions are required");
        }
        UUID userId;
        try {
            userId = UUID.fromString(principal.getName());
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Invalid WebSocket user", exception);
        }

        String[] parts = destination.split("/");
        if (parts.length != 4 || !"topic".equals(parts[1])) {
            throw new AccessDeniedException("Subscription destination is not allowed");
        }
        UUID resourceId;
        try {
            resourceId = UUID.fromString(parts[3]);
        } catch (IllegalArgumentException exception) {
            throw new AccessDeniedException("Subscription destination is invalid", exception);
        }

        boolean allowed = switch (parts[2]) {
            case "listener", "chat" -> userId.equals(resourceId);
            case "call" -> callSessionRepository.existsParticipant(resourceId, userId);
            case "rooms" -> roomParticipantRepository.existsByRoomIdAndUserIdAndActiveTrue(resourceId, userId);
            default -> false;
        };
        if (!allowed) {
            throw new AccessDeniedException("Not authorized for this WebSocket destination");
        }
    }
}
