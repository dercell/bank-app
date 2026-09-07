package ru.yandex.practicum.accounts.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.yandex.practicum.accounts.model.CashAction;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CashOpDto {

    @JsonProperty
    private CashAction action;

    @JsonProperty
    private String accNumber;

    @JsonProperty
    private BigDecimal sum;

}
