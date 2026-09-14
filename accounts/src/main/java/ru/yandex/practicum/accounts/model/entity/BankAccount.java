package ru.yandex.practicum.accounts.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bank_account")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_num")
    private String accountNum;

    @Column(name = "owner_login")
    private String login;

    @Column(name = "balance")
    private BigDecimal balance;

}
