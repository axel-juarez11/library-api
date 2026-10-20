package com.axel.libraryapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @Pattern(regexp = "^[0-9]{10}$", message = "debe tener 10 digitos") String phone
) {}
