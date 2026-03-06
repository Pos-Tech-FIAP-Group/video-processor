package com.fiap.fiapx.auth.adapters.driver.api.exceptionhandler;

import com.fiap.fiapx.auth.core.application.exception.InvalidCredentialsException;
import com.fiap.fiapx.auth.core.application.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("InvalidCredentialsException → 401 e body com message")
    void invalidCredentials_retorna_401_e_message() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Credenciais inválidas");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleInvalidCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().message()).isEqualTo("Credenciais inválidas");
    }

    @Test
    @DisplayName("UserAlreadyExistsException → 400")
    void userAlreadyExists_retorna_400() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("Username já existe: foo");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleUserAlreadyExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("Username já existe: foo");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException → 400 com mensagem dos campos")
    void methodArgumentNotValid_retorna_400_com_campos() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(null, "target");
        bindingResult.addError(new FieldError("target", "username", "must not be blank"));
        bindingResult.addError(new FieldError("target", "email", "must be a valid email"));
        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().message())
                .contains("username")
                .contains("must not be blank")
                .contains("email")
                .contains("must be a valid email");
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String arg) {
    }
}
