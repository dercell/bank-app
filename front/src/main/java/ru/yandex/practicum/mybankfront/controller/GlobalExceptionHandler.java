package ru.yandex.practicum.mybankfront.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

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
        ObjectMapper objectMapper = new ObjectMapper();
        String msg;
        try {
            ServiceResultDto res = objectMapper.readValue(exception.getResponseBodyAsString(), ServiceResultDto.class);

            if ("AccountNotExists".equals(res.getResultCode())) {
                return "profile";
            } else {
                msg = res.getMessage();
            }
        } catch (JacksonException ex) {
            msg = exception.getResponseBodyAsString();
        }

        if (oidcUser != null) {
            model.addAttribute("username", oidcUser.getPreferredUsername());
        }
        model.addAttribute("errors", List.of(msg));
        return "main";
    }

    @ExceptionHandler(Exception.class)
    public String handler500(Model model, Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        model.addAttribute("errors", List.of(ex.getMessage()));
        return "main";
    }
}
