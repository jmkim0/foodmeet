package im.jmk.foodmeet.user.controller;

import im.jmk.foodmeet.common.response.Response;
import im.jmk.foodmeet.user.dto.UserDto;
import im.jmk.foodmeet.user.dto.UserPatchDto;
import im.jmk.foodmeet.user.dto.UserPostDto;
import im.jmk.foodmeet.user.entity.User;
import im.jmk.foodmeet.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
@Validated
public class UserController {
    private final UserService userService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<Response<UserDto>> postUser(@Valid @RequestBody UserPostDto userPostDto) {
        UserDto userDto = userService.createUser(userPostDto);

        return new ResponseEntity<>(Response.of(userDto),
                HttpStatus.CREATED);
    }

    @GetMapping("/duplicate-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void getDuplicateCheck(@NotBlank @RequestParam String username) {
        userService.verifyExistsUsername(username);
    }

    @GetMapping("/password-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void getPasswordCheck(@NotBlank @RequestParam String password) {
        userService.checkPassword(password);
    }

    @GetMapping("/login-status")
    public ResponseEntity<Response<UserDto>> getLoginStatus(@AuthenticationPrincipal User user) {
        return new ResponseEntity<>(Response.of(user.toDto()), HttpStatus.OK);
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<Response<UserDto>> getUser(
            @PathVariable("user-id") @Positive int userId) {
        UserDto userDto = userService.findUser(userId);
        return new ResponseEntity<>(Response.of(userDto)
                , HttpStatus.OK);
    }

    @PatchMapping("/{user-id}")
    public ResponseEntity<Response<UserDto>> edit(
            @PathVariable("user-id") @Positive int userId, @RequestBody UserPatchDto userPatchDto) {
        UserDto userDto = userService.editUser(userId, userPatchDto);

        return new ResponseEntity<>(Response.of(userDto)
                , HttpStatus.OK);
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable("user-id") @Positive int userId) {
        userService.deleteUser(userId);
    }
}

