package ru.yandex.practicum.accounts.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "username")
    private String username;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Column(name = "balance")
    private Long balance;

}
