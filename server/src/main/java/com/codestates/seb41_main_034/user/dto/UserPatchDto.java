package com.codestates.seb41_main_034.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPatchDto {
    @Size(min = 1)
    private String displayName;

    @NotBlank(message = "영문, 숫자를 포함한 8자 이상 비밀번호를 입력해주세요.")
    private String oldPassword;

    @NotBlank(message = "영문, 숫자를 포함한 8자 이상 비밀번호를 입력해주세요.")
    private String newPassword;

}