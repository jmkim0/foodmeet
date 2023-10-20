package im.jmk.foodmeet.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class QuestionPatchDto {

    @NotBlank
    private String body;

}
