package ru.yandex.practicum.accounts.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "username")
    private String username;

    @Column(name = "birthdate")
    private LocalDate birthDate;

    @Builder.Default
    @OneToMany
    @JoinColumn(name = "user_id")
    private List<BankAccount> accountList = new ArrayList<>();

}
