package ru.yandex.practicum.mybankfront.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountInfoDto {

    @JsonProperty
    private String login;

    @JsonProperty
    private String username;

    @JsonProperty
    private List<AccountDto> accounts;

}
