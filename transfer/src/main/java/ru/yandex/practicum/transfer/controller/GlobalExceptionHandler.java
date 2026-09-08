package ru.yandex.practicum.transfer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ServiceResultDto> handleWebClientResponseException(
            WebClientResponseException exception
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        ServiceResultDto res;
        try {
            res = objectMapper.readValue(exception.getResponseBodyAsString(), ServiceResultDto.class);
        } catch (JacksonException ex) {
            res = new ServiceResultDto(exception.getClass().getSimpleName(), exception.getResponseBodyAsString());
        }

        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResultDto> handler500(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(new ServiceResultDto(ex.getClass().getSimpleName(), ex.getMessage()));
    }


}
