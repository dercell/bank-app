package ru.yandex.practicum.transfer.controller;

import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;
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
    public ServiceResultDto transfer(
           @RequestBody TransferDto body
    ) {
        return transferSerivce.makeTransfer(body);
    }

}
