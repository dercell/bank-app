package ru.yandex.practicum.cash.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.cash.dto.ServiceResultDto;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ServiceResultDto> handleWebClientResponseException(
            WebClientResponseException exception
    ) {
        String body = exception.getResponseBodyAsString();

        return ResponseEntity.badRequest().body(new ServiceResultDto(body));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResultDto> handler500(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(new ServiceResultDto(ex.getMessage()));
    }

}
