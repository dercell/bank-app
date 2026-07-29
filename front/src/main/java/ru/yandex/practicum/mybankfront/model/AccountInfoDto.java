package ru.yandex.practicum.mybankfront.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoDto {

    @JsonProperty
    private AccountDto curAccount;

    @JsonProperty
    private List<AccountStripped> accounts;


}
