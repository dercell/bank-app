package ru.yandex.practicum.accounts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AccountDto {

    @JsonProperty
    private Account curAccount;

    @JsonProperty
    private List<AccountStripped> accounts;


}
