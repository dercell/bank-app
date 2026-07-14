package ru.yandex.practicum.mybankfront.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class AccountInfoDto {

    @JsonProperty
    private AccountDto curAccount;

    @JsonProperty
    private List<AccountStripped> accounts;


}
