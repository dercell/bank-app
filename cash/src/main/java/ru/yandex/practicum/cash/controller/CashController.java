package ru.yandex.practicum.cash.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.service.CashService;


@Slf4j
@RestController
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PutMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('cash.write')")
    public ResponseEntity<Void> chargeSum(@Valid @RequestBody CashOpDto body) {
        log.info("Get request for {} ", body);
        cashService.chargeSum(body);
        return ResponseEntity.noContent().build();
    }

}
