package im.jmk.foodmeet.question;

import im.jmk.foodmeet.common.JsonListHelper;
import im.jmk.foodmeet.common.exception.BusinessLogicException;
import im.jmk.foodmeet.common.exception.ExceptionCode;
import im.jmk.foodmeet.common.response.PaginatedData;
import im.jmk.foodmeet.product.ProductService;
import im.jmk.foodmeet.product.entity.Product;
import im.jmk.foodmeet.question.dto.AnswerRequestDto;
import im.jmk.foodmeet.question.dto.QuestionDto;
import im.jmk.foodmeet.question.dto.QuestionPatchDto;
import im.jmk.foodmeet.question.dto.QuestionPostDto;
import im.jmk.foodmeet.question.entity.Answer;
import im.jmk.foodmeet.question.entity.Question;
import im.jmk.foodmeet.user.entity.User;
import im.jmk.foodmeet.user.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class QuestionFacade {

    private final QuestionService questionService;

    private final ProductService productService;

    private final UserService userService;

    private final JsonListHelper helper;

    public QuestionDto createQuestion(QuestionPostDto questionPostDto) {
        // 상품 정보 조회
        Product product = productService.readProduct(questionPostDto.getProductId());

        // 엔티티 객체 생성
        Question question = questionService.createQuestion(questionPostDto);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // DTO 매핑 후 반환
        return question.toDto(helper, product, Map.of(user.getId(), user.getDisplayName()));
    }

    public QuestionDto readQuestion(long questionId) {
        // 문의 조회
        Question question = questionService.readQuestion(questionId);

        Set<Integer> userIds = new HashSet<>();
        userIds.add(question.getCreatedBy());
        Optional.ofNullable(question.getAnswer()).map(Answer::getCreatedBy).ifPresent(userIds::add);

        // 상품 정보 조회
        Product product = productService.readProduct(question.getProductId());

        Map<Integer, String> idNameMap = userService.getVerifiedUsers(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getMaskedName));

        // DTO 매핑 후 반환
        return question.toDto(helper, product, idNameMap);
    }

    public PaginatedData<QuestionDto> readProductQuestions(int productId, Pageable pageable) {
        // 상품 정보 조회
        Product product = productService.readProduct(productId);

        // 문의 목록 Page 조회
        Page<Question> questionPage = questionService.readProductQuestions(productId, pageable);
        Set<Integer> userIds = new HashSet<>();
        questionPage.getContent().forEach(question -> {
            userIds.add(question.getCreatedBy());
            Optional.ofNullable(question.getAnswer()).map(Answer::getCreatedBy).ifPresent(userIds::add);
        });

        Map<Integer, String> idNameMap = userService.getVerifiedUsers(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getMaskedName));

        // DTO 매핑 후 반환
        return PaginatedData.of(questionPage.map(question -> question.toDto(helper, product, idNameMap)));
    }

    public PaginatedData<QuestionDto> readQuestionHistory(LocalDate from, LocalDate to, Pageable pageable) {
        int createdBy = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        // from, to 기본값 설정
        from = Optional.ofNullable(from).orElse(LocalDate.of(2023, 1, 1));
        to = Optional.ofNullable(to).orElse(LocalDate.now());

        // 회원의 문의 목록 Page 조회
        Page<Question> questionPage = questionService.readQuestionHistory(createdBy, from, to, pageable);

        // 문의 목록에 있는 상품 정보를 한 번에 조회
        Set<Integer> productIds = questionPage.get().map(Question::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = productService.getVerifiedProducts(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Set<Integer> userIds = new HashSet<>();
        questionPage.getContent().forEach(question -> {
            userIds.add(question.getCreatedBy());
            Optional.ofNullable(question.getAnswer()).map(Answer::getCreatedBy).ifPresent(userIds::add);
        });

        Map<Integer, String> idNameMap = userService.getVerifiedUsers(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        user -> user.getId() == createdBy ? user.getDisplayName() : user.getMaskedName()
                ));


        // DTO 매핑 후 반환
        return PaginatedData.of(
                questionPage.map(question ->
                        question.toDto(helper, productMap.get(question.getProductId()), idNameMap)));
    }

    public QuestionDto updateQuestion(long questionId, QuestionPatchDto questionPatchDto) {
        Question question = questionService.readQuestion(questionId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != question.getCreatedBy()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        // 문의 수정 후 반환
        Question updatedQuestion = questionService.updateQuestion(questionId, questionPatchDto);

        // DTO 매핑 후 반환
        return updatedQuestion.toDto();
    }

    public void deleteQuestion(long questionId) {
        Question question = questionService.readQuestion(questionId);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!user.getRoles().contains("ADMIN") && user.getId() != question.getCreatedBy()) {
            throw new BusinessLogicException(ExceptionCode.AUTH_FORBIDDEN);
        }

        // 문의 삭제
        questionService.deleteQuestion(questionId);
    }

    public QuestionDto createAnswer(long questionId, AnswerRequestDto answerRequestDto) {
        // 답변 추가
        Question question = questionService.createAnswer(questionId, answerRequestDto);

        // DTO 매핑 후 반환
        return question.toDto();
    }

    public QuestionDto updateAnswer(long questionId, AnswerRequestDto answerRequestDto) {
        // 답변 수정
        Question question = questionService.updateAnswer(questionId, answerRequestDto);

        // DTO 매핑 후 반환
        return question.toDto();
    }

}
