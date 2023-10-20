package im.jmk.foodmeet.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AnswerRequestDto {

    @NotBlank
    private String body;

}
