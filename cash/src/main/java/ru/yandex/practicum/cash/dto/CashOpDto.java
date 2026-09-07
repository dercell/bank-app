package ru.yandex.practicum.cash.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.*;


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
    @Positive
    private BigDecimal sum;

}
