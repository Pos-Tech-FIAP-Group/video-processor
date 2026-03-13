package com.fiap.fiapx.auth.adapters.driver.api.config;

import com.fiap.fiapx.auth.core.application.ports.PasswordEncoderPort;
import com.fiap.fiapx.auth.core.application.ports.TokenProviderPort;
import com.fiap.fiapx.auth.core.application.ports.UserRepositoryPort;
import com.fiap.fiapx.auth.core.application.usecases.AuthenticateUserUseCase;
import com.fiap.fiapx.auth.core.application.usecases.GetUserByIdUseCase;
import com.fiap.fiapx.auth.core.application.usecases.GetUserByUuidUseCase;
import com.fiap.fiapx.auth.core.application.usecases.RegisterUserUseCase;
import com.fiap.fiapx.auth.core.application.usecases.ValidateTokenUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserRepositoryPort userRepository,
                                                    PasswordEncoderPort passwordEncoder) {
        return new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserRepositoryPort userRepository,
                                                           TokenProviderPort tokenProvider,
                                                           PasswordEncoderPort passwordEncoder,
                                                           @Value("${jwt.expiration}") long expiresInMs) {
        return new AuthenticateUserUseCase(userRepository, tokenProvider, passwordEncoder, expiresInMs);
    }

    @Bean
    public ValidateTokenUseCase validateTokenUseCase(TokenProviderPort tokenProvider) {
        return new ValidateTokenUseCase(tokenProvider);
    }

    @Bean
    public GetUserByIdUseCase getUserByIdUseCase(UserRepositoryPort userRepository) {
        return new GetUserByIdUseCase(userRepository);
    }

    @Bean
    public GetUserByUuidUseCase getUserByUuidUseCase(UserRepositoryPort userRepository) {
        return new GetUserByUuidUseCase(userRepository);
    }
}
