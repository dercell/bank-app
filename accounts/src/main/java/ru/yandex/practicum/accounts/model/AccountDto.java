package ru.yandex.practicum.accounts.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    @JsonProperty
    private Account curAccount;

    @JsonProperty
    private List<AccountStripped> accounts;


}
