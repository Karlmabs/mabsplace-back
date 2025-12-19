package com.mabsplace.mabsplaceback.config;

import com.mabsplace.mabsplaceback.domain.entities.User;
import com.mabsplace.mabsplaceback.domain.repositories.UserRepository;
import com.mabsplace.mabsplaceback.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Intercepts STOMP CONNECT frames to authenticate WebSocket sessions using JWT.
 * Sets the Principal name to the numeric userId so that convertAndSendToUser(userId, ...) works.
 */
@Component
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthChannelInterceptor.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                String authHeader = getFirstNativeHeader(accessor, "Authorization");
                if (authHeader == null || authHeader.isBlank()) {
                    // Also try lowercase header (some clients send it that way)
                    authHeader = getFirstNativeHeader(accessor, "authorization");
                }

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    if (jwtUtils.validateJwtToken(token)) {
                        String username = jwtUtils.getUserNameFromJwtToken(token);
                        Optional<User> userOpt = userRepository.findByUsername(username);
                        if (userOpt.isPresent()) {
                            User user = userOpt.get();
                            Principal principal = new Principal() {
                                @Override
                                public String getName() {
                                    // Use user ID as Principal name so /user/{id}/ works
                                    return user.getId().toString();
                                }
                            };
                            accessor.setUser(principal);
                            logger.info("WebSocket CONNECT authenticated for user {} (id={})", username, user.getId());
                        } else {
                            logger.warn("WebSocket CONNECT: user not found for username {}", username);
                        }
                    } else {
                        logger.warn("WebSocket CONNECT: invalid JWT token");
                    }
                } else {
                    logger.debug("WebSocket CONNECT without Authorization header");
                }
            } catch (Exception e) {
                logger.error("WebSocket CONNECT authentication failed", e);
            }
        }

        return message;
    }

    @Nullable
    private String getFirstNativeHeader(StompHeaderAccessor accessor, String name) {
        List<String> values = accessor.getNativeHeader(name);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return null;
    }
}

