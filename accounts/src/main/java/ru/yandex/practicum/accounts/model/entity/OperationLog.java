package ru.yandex.practicum.accounts.model.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.accounts.model.OperationStatus;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "operation_log")
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "initiator")
    private String initiator;

    @Column(name = "target")
    private String target;

}
