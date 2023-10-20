package im.jmk.foodmeet.question.entity;

import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.auditing.entity.Auditable;
import im.jmk.foodmeet.product.entity.Product;
import im.jmk.foodmeet.question.dto.AnswerDto;
import im.jmk.foodmeet.question.dto.QuestionDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Entity
@Table(indexes = {
        @Index(name = "idx_question_product_id", columnList = "productId"),
        @Index(name = "idx_question_created_by_created_at", columnList = "createdBy, createdAt")
})
public class Question extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int productId;

    private String body;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "question")
    private Answer answer;

    public QuestionDto toDto(JsonListHelper helper, Product product, Map<Integer, String> idNameMap) {
        List<String> urlList = helper.jsonToList(product.getImageUrls());
        String productImageUrl = urlList.isEmpty() ? null : urlList.get(0);
        AnswerDto answerDto = Optional.ofNullable(answer)
                .map(_answer -> _answer.toDto(idNameMap.get(_answer.getCreatedBy()))).orElse(null);

        return new QuestionDto(id, productId, product.getName(), productImageUrl, body, answerDto,
                getCreatedBy(), idNameMap.get(getCreatedBy()), getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

    public QuestionDto toDto() {
        AnswerDto answerDto = Optional.ofNullable(answer).map(Answer::toDto).orElse(null);

        return new QuestionDto(id, productId, null, null, body, answerDto,
                getCreatedBy(), null, getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

}
