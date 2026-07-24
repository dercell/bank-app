package ru.yandex.practicum.mybankfront.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public String handleWebClientResponseException(
            WebClientResponseException exception,
            Model model,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {

        String body = exception.getResponseBodyAsString();
        model.addAttribute("errors", List.of(body));

        if (oidcUser != null) {
            model.addAttribute("username", oidcUser.getPreferredUsername());
        }

        return "main";
    }

    @ExceptionHandler(Exception.class)
    public String handler500(Model model, Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        model.addAttribute("errors", List.of(ex.getMessage()));
        return "main";
    }
}
