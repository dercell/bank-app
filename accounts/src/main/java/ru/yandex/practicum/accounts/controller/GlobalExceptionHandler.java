package ru.yandex.practicum.accounts.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.accounts.exceptions.BusinessException;
import ru.yandex.practicum.accounts.model.dto.ServiceResultDto;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ServiceResultDto> handleBusinessException(BusinessException be) {
        log.error("Business error: {}", be.getMessage(), be);
        return ResponseEntity.badRequest().body(new ServiceResultDto(be.getClass().getSimpleName(), be.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ServiceResultDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("Bad request data: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest().body(new ServiceResultDto(ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResultDto> handler500(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(new ServiceResultDto(ex.getClass().getSimpleName(), ex.getMessage()));
    }
}
