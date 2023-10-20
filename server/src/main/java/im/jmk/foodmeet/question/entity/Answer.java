package im.jmk.foodmeet.question.entity;

import im.jmk.foodmeet.common.auditing.entity.Auditable;
import im.jmk.foodmeet.question.dto.AnswerDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Answer extends Auditable {

    @Id
    private Long questionId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(nullable = false)
    private String body;

    public Answer(Question question, String body) {
        this.question = question;
        this.body = body;
    }

    public AnswerDto toDto(String createdByName) {
        return new AnswerDto(body, getCreatedBy(), createdByName, getModifiedBy(), getCreatedAt(), getModifiedAt());
    }

    public AnswerDto toDto() {
        return toDto(null);
    }

}
