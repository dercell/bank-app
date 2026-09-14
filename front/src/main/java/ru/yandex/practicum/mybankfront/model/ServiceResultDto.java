package ru.yandex.practicum.mybankfront.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServiceResultDto {

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("message")
    private String message;

    public ServiceResultDto(String message){
        this.resultCode = "success";
        this.message = message;
    }

}
