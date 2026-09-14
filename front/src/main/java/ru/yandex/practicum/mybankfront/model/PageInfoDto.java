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
public class PageInfoDto {

    @JsonProperty
    private UserProfileDto userProfileDto;

    @JsonProperty
    private List<AccountDto> curAccounts;

    @JsonProperty
    private List<UserAccountInfoDto> accounts;


}
