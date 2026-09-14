package ru.yandex.practicum.accounts.model.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@RequiredArgsConstructor
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
