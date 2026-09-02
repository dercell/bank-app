package ru.yandex.practicum.cash.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
public class ServiceResultDto {

    @JsonProperty("message")
    private String message;

}
