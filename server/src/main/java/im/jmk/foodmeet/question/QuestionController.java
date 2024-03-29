package im.jmk.foodmeet.question;

import im.jmk.foodmeet.common.response.PaginatedData;
import im.jmk.foodmeet.common.response.Response;
import im.jmk.foodmeet.question.dto.AnswerRequestDto;
import im.jmk.foodmeet.question.dto.QuestionDto;
import im.jmk.foodmeet.question.dto.QuestionPatchDto;
import im.jmk.foodmeet.question.dto.QuestionPostDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@Validated
@RequestMapping("/api/v1")
public class QuestionController {

    private final QuestionFacade questionFacade;

    @PostMapping("/question")
    public ResponseEntity<Response<QuestionDto>> postQuestion(@Valid @RequestBody QuestionPostDto questionPostDto) {
        return new ResponseEntity<>(Response.of(questionFacade.createQuestion(questionPostDto)), HttpStatus.CREATED);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<Response<QuestionDto>> getQuestion(@Positive @PathVariable long questionId) {
        return new ResponseEntity<>(Response.of(questionFacade.readQuestion(questionId)), HttpStatus.OK);
    }

    @GetMapping("/product/{productId}/question")
    public ResponseEntity<Response<PaginatedData<QuestionDto>>> getProductQuestions(
            @Positive @PathVariable int productId,
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(
                Response.of(questionFacade.readProductQuestions(productId, pageable)), HttpStatus.OK);
    }

    @GetMapping("/question/question-history")
    public ResponseEntity<Response<PaginatedData<QuestionDto>>> getQuestionHistory(
            @PastOrPresent @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @PastOrPresent @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(
                Response.of(questionFacade.readQuestionHistory(from, to, pageable)), HttpStatus.OK);
    }

    @PatchMapping("/question/{questionId}")
    public ResponseEntity<Response<QuestionDto>> patchQuestion(
            @Positive @PathVariable long questionId, @Valid @RequestBody QuestionPatchDto questionPatchDto) {
        return new ResponseEntity<>(
                Response.of(questionFacade.updateQuestion(questionId, questionPatchDto)), HttpStatus.OK);
    }

    @DeleteMapping("/question/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@Positive @PathVariable long questionId) {
        questionFacade.deleteQuestion(questionId);
    }

    @PostMapping("/question/{questionId}/answer")
    public ResponseEntity<Response<QuestionDto>> postAnswer(
            @Positive @PathVariable long questionId, @Valid @RequestBody AnswerRequestDto answerRequestDto) {
        return new ResponseEntity<>(
                Response.of(questionFacade.createAnswer(questionId, answerRequestDto)), HttpStatus.OK);
    }

    @PatchMapping("/question/{questionId}/answer")
    public ResponseEntity<Response<QuestionDto>> patchAnswer(
            @Positive @PathVariable long questionId, @Valid @RequestBody AnswerRequestDto answerRequestDto) {
        return new ResponseEntity<>(
                Response.of(questionFacade.updateAnswer(questionId, answerRequestDto)), HttpStatus.OK);
    }

}
