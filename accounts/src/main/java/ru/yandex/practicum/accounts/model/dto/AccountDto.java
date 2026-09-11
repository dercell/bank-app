package ru.yandex.practicum.accounts.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    @JsonProperty
    private String accountNumber;

    @JsonProperty
    private BigDecimal balance;

}
