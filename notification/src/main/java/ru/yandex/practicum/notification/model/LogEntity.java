package ru.yandex.practicum.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LogEntity {

    @JsonProperty
    private SourceService sourceService;

    @JsonProperty
    private String message;

}
