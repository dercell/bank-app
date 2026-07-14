package ru.yandex.practicum.accounts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class AccountStripped {

    @JsonProperty
    private String login;

    @JsonProperty
    private String username;

}
