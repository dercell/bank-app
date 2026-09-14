package ru.yandex.practicum.mybankfront.model.client;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.yandex.practicum.mybankfront.model.CashAction;

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
