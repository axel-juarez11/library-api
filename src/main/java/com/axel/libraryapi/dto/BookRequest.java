package com.axel.libraryapi.dto;

import jakarta.validation.constraints.*;

public record BookRequest(
    @NotBlank String title,
    @NotBlank String author,
    @NotBlank @Size(min = 10, max = 17) String isbn,
    String category,
    @NotNull @Min(1) Integer totalCopies
) {}
