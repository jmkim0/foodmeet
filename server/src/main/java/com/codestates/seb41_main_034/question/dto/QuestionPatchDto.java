package com.codestates.seb41_main_034.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class QuestionPatchDto {

    @NotBlank
    private String body;

}
