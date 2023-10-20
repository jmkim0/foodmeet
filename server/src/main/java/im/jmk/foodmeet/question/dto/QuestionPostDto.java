package im.jmk.foodmeet.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class QuestionPostDto {

    @NotNull
    @Positive
    private int productId;

    @NotBlank
    private String body;

}
