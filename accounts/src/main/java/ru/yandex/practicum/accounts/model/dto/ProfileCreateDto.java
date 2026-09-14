package ru.yandex.practicum.accounts.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreateDto {

    @JsonProperty
    private String login;

    @JsonProperty
    private String username;

    @JsonProperty
    private LocalDate birthDate;

}
