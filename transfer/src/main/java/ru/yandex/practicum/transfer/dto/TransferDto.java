package ru.yandex.practicum.transfer.dto;


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
public class TransferDto {

    @JsonProperty
    private String fromAcc;

    @JsonProperty
    private String toAcc;

    @Positive
    @JsonProperty
    private BigDecimal sum;



}
