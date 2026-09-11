package ru.yandex.practicum.mybankfront.model.client;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TransferDto {

    @JsonProperty
    private String fromAcc;

    @JsonProperty
    private String toAcc;

    @JsonProperty
    private BigDecimal sum;



}
