package com.axel.libraryapi.dto;

import jakarta.validation.constraints.NotNull;

public record LoanRequest(
        @NotNull Long bookId,
        @NotNull Long memberId
) {}
