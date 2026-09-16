package ru.yandex.practicum.transfer.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class LogEntity {

    private SourceService sourceService;

    private String message;

}
