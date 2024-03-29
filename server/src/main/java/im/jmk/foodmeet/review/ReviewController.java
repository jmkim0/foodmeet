package im.jmk.foodmeet.review;

import im.jmk.foodmeet.common.response.PaginatedData;
import im.jmk.foodmeet.common.response.Response;
import im.jmk.foodmeet.review.dto.ReviewDto;
import im.jmk.foodmeet.review.dto.ReviewPatchDto;
import im.jmk.foodmeet.review.dto.ReviewPostDto;
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
public class ReviewController {

    private final ReviewFacade reviewFacade;

    @PostMapping("/review")
    public ResponseEntity<Response<ReviewDto>> postReview(
            @Valid @RequestBody ReviewPostDto reviewPostDto
    ) {
        return new ResponseEntity<>(Response.of(reviewFacade.createReview(reviewPostDto)), HttpStatus.CREATED);
    }

    @GetMapping("/review/{reviewId}")
    public ResponseEntity<Response<ReviewDto>> getReview(@Positive @PathVariable long reviewId) {
        return new ResponseEntity<>(Response.of(reviewFacade.readReview(reviewId)), HttpStatus.OK);
    }

    @GetMapping("/product/{productId}/review")
    public ResponseEntity<Response<PaginatedData<ReviewDto>>> getProductReviews(
            @Positive @PathVariable int productId,
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(
                Response.of(reviewFacade.readProductReviews(productId, pageable)), HttpStatus.OK);
    }

    @GetMapping("/review/review-history")
    public ResponseEntity<Response<PaginatedData<ReviewDto>>> getReviewHistory(
            @PastOrPresent @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @PastOrPresent @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to,
            @PageableDefault(sort = "id", direction = Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(
                Response.of(reviewFacade.readReviewHistory(from, to, pageable)), HttpStatus.OK);
    }

    @PatchMapping("/review/{reviewId}")
    public ResponseEntity<Response<ReviewDto>> patchReview(
            @Positive @PathVariable long reviewId,
            @Valid @RequestBody ReviewPatchDto reviewPatchDto
    ) {
        return new ResponseEntity<>(
                Response.of(reviewFacade.updateReview(reviewId, reviewPatchDto)), HttpStatus.OK);
    }

    @DeleteMapping("/review/{reviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@Positive @PathVariable long reviewId) {
        reviewFacade.deleteReview(reviewId);
    }

}
