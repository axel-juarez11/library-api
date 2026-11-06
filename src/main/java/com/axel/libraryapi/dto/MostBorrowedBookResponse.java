package com.axel.libraryapi.dto;

public record MostBorrowedBookResponse(
        Long bookId,
        String title,
        String author,
        long timesBorrowed
) {}
