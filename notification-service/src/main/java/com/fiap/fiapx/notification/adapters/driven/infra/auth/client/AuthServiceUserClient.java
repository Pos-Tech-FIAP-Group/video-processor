package com.fiap.fiapx.notification.adapters.driven.infra.auth.client;

import com.fiap.fiapx.notification.core.application.ports.GetUserByUuidPort;
import com.fiap.fiapx.notification.core.domain.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * Adapter que chama o auth-service GET /internal/users/by-uuid/{uuid} para obter o usuario.
 * Sem JWT (endpoint interno para service-to-service).
 */
@Component
public class AuthServiceUserClient implements GetUserByUuidPort {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceUserClient.class);

    private final RestTemplate restTemplate;
    private final String authServiceBaseUrl;

    public AuthServiceUserClient(
            RestTemplate restTemplate,
            @Value("${auth.service-url}") String authServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.authServiceBaseUrl = authServiceBaseUrl.endsWith("/") ? authServiceBaseUrl : authServiceBaseUrl + "/";
    }

    @Override
    public Optional<UserInfo> findByUserUuid(String userUuid) {
        if (userUuid == null || userUuid.isBlank()) {
            return Optional.empty();
        }
        String url = authServiceBaseUrl + "internal/users/by-uuid/" + userUuid;
        try {
            ResponseEntity<AuthUserResponse> response = restTemplate.getForEntity(url, AuthUserResponse.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AuthUserResponse body = response.getBody();
                return Optional.of(new UserInfo(body.email(), body.username()));
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound e) {
            logger.debug("User not found for uuid: {}", userUuid);
            return Optional.empty();
        } catch (Exception e) {
            logger.warn("Failed to get user by uuid: {}", userUuid, e);
            return Optional.empty();
        }
    }

    /**
     * Formato da resposta do auth-service GET /internal/users/by-uuid/{uuid}.
     */
    private record AuthUserResponse(String id, String userUuid, String username, String email, boolean enabled) {
    }
}
