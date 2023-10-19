package com.codestates.seb41_main_034.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AnswerRequestDto {

    @NotBlank
    private String body;

}
