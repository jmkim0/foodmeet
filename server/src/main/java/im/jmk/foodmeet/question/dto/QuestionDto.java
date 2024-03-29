package im.jmk.foodmeet.question.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QuestionDto {

    private long id;

    private int productId;

    @JsonInclude(Include.NON_NULL)
    private String productName;

    @JsonInclude(Include.NON_NULL)
    private String productImageUrl;

    private String body;

    private AnswerDto answer;

    private int createdBy;

    @JsonInclude(Include.NON_NULL)
    private String createByName;

    private int modifiedBy;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

}
