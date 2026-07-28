package ru.yandex.practicum.transfer.controller;

import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.transfer.service.TransferService;

@Slf4j
@Validated
@RestController
@RequestMapping(("/transfer"))
public class TransferController {

    private final TransferService transferSerivce;

    public TransferController(TransferService transferSerivce) {
        this.transferSerivce = transferSerivce;
    }

    @PutMapping("/submit")
    @PreAuthorize("hasRole('USER') && hasAuthority('transfer.write')")
    public String transfer(
            @RequestParam("from") String fromLogin,
            @RequestParam("to") String toLogin,
            @RequestParam("sum") @Min(0) int sum
    ) {
        return transferSerivce.makeTransfer(fromLogin, toLogin, sum);
    }

}
