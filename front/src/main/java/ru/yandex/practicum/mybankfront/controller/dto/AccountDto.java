package ru.yandex.practicum.mybankfront.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
public class AccountDto {

    @JsonProperty
    private String login;

    @JsonProperty
    private String username;

    @JsonProperty
    private LocalDate birthDate;

    @JsonProperty
    private Long balance;
}
