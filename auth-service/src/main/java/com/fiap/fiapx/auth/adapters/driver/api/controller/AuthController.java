package com.fiap.fiapx.auth.adapters.driver.api.controller;

import com.fiap.fiapx.auth.adapters.driver.api.dto.request.LoginRequest;
import com.fiap.fiapx.auth.adapters.driver.api.dto.request.RegisterRequest;
import com.fiap.fiapx.auth.adapters.driver.api.dto.response.AuthResponse;
import com.fiap.fiapx.auth.adapters.driver.api.dto.response.UserResponse;
import com.fiap.fiapx.auth.core.application.usecases.AuthenticateUserUseCase;
import com.fiap.fiapx.auth.core.application.usecases.GetUserByIdUseCase;
import com.fiap.fiapx.auth.core.application.usecases.RegisterUserUseCase;
import com.fiap.fiapx.auth.core.application.usecases.ValidateTokenUseCase;
import com.fiap.fiapx.auth.core.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          AuthenticateUserUseCase authenticateUserUseCase,
                          ValidateTokenUseCase validateTokenUseCase,
                          GetUserByIdUseCase getUserByIdUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.validateTokenUseCase = validateTokenUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserUseCase.execute(
                request.username(),
                request.email(),
                request.password()
        );
        UserResponse response = toUserResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUserUseCase.execute(request.username(), request.password());
        AuthResponse response = AuthResponse.of(result.token(), result.expiresInMs(), result.username());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestParam("token") String token) {
        return validateTokenUseCase.execute(token)
                .map(subject -> ResponseEntity.ok(new ValidateResponse(true, subject)))
                .orElse(ResponseEntity.ok(new ValidateResponse(false, null)));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") String id) {
        return getUserByIdUseCase.execute(id)
                .map(user -> ResponseEntity.ok(toUserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled()
        );
    }

    public record ValidateResponse(boolean valid, String subject) {
    }
}
