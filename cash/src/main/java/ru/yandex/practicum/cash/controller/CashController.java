package ru.yandex.practicum.cash.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cash.service.CashService;


@Slf4j
@RestController
@RequestMapping("/cash")
public class CashController {

    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PutMapping("/{login}")
    @PreAuthorize("hasRole('USER') && hasAuthority('cash.write')")
    public ResponseEntity<Void> chargeSum(
            @PathVariable("login") String login,
            @RequestParam("action") String action,
            @RequestParam("sum") Integer sum
    ) {
        log.info("Get request for {} {} from {} ", action, sum, login);
        cashService.chargeSum(login, action, sum);
        return ResponseEntity.noContent().build();
    }

}
