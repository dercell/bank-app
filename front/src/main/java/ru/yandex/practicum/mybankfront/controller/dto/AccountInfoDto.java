package ru.yandex.practicum.mybankfront.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder
public class AccountInfoDto {

    @JsonProperty
    private AccountDto curAccount;

    @JsonProperty
    private List<AccountStripped> accounts;


}
