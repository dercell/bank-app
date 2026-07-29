package ru.yandex.practicum.mybankfront.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
