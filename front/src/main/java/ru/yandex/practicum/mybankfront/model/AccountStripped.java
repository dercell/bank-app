package ru.yandex.practicum.mybankfront.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountStripped {

    @JsonProperty
    private String login;

    @JsonProperty("username")
    private String name;

}
