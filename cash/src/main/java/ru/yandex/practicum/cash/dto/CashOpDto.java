package ru.yandex.practicum.cash.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.validation.annotation.Validated;


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

    @Positive(message = "Сумма должна быть больше 0")
    @JsonProperty
    private BigDecimal sum;

}
