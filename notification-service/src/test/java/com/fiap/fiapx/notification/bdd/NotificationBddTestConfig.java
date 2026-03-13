package com.fiap.fiapx.notification.bdd;

import com.fiap.fiapx.notification.core.application.ports.GetUserByUuidPort;
import com.fiap.fiapx.notification.core.domain.model.UserInfo;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class NotificationBddTestConfig {

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }

    @Bean
    @Primary
    public GetUserByUuidPort getUserByUuidPort() {
        return userUuid -> Optional.of(new UserInfo("bdd-user@example.com", "BddUser"));
    }
}
