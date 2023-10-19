package com.codestates.seb41_main_034.useraddress.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserAddressPostDto {

    @NotBlank
    private String recipient;

    @NotBlank
    private String address;

    private boolean isPrimary;

}
