package ru.yandex.practicum.accounts.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountStripped {

    @JsonProperty
    private String login;

    @JsonProperty
    private String username;

}
