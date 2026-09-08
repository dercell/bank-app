package ru.yandex.practicum.accounts.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.accounts.exceptions.BusinessException;
import ru.yandex.practicum.accounts.model.dto.ServiceResultDto;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ServiceResultDto> handleValidationRequest(MethodArgumentNotValidException ex) {
        log.error("Request body validation error: {}", ex.getMessage(), ex);
        String errors = Arrays.stream(ex.getDetailMessageArguments())
                .map(String.class::cast).map(String::trim).filter(s -> !s.isEmpty())
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest().body(new ServiceResultDto(ex.getClass().getSimpleName(), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceResultDto> handler500(Exception ex) {
        log.error("Internal server error: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(new ServiceResultDto(ex.getClass().getSimpleName(), ex.getMessage()));
    }
}
