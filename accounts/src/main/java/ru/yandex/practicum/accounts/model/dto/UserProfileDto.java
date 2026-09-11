package ru.yandex.practicum.accounts.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    @JsonProperty
    private String login;

    @JsonProperty
    private String username;

    @JsonProperty
    private LocalDate birthDate;

}
