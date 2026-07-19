package ru.yandex.practicum.accounts.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.model.AccountDto;
import ru.yandex.practicum.accounts.service.AccountService;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private AccountService accountService;

    @GetMapping("/info")
    public AccountDto getMyAccount(Authentication authentication) {

        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        Jwt jwt = token.getToken();
        String username = jwt.getClaim("preferred_username");


        return accountService.getAccountInfo(username);
    }

}
