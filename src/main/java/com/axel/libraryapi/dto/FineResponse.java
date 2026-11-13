package com.axel.libraryapi.dto;

import com.axel.libraryapi.entity.Fine;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FineResponse(
        Long id,
        Long loanId,
        Long memberId,
        String memberName,
        String bookTitle,
        long daysOverdue,
        BigDecimal amount,
        LocalDate createdAt,
        boolean paid,
        LocalDate paidAt
) {
    public static FineResponse from(Fine f, LocalDate today) {
        return new FineResponse(
                f.getId(),
                f.getLoan().getId(),
                f.getLoan().getMember().getId(),
                f.getLoan().getMember().getName(),
                f.getLoan().getBook().getTitle(),
                f.getLoan().daysOverdueOn(today),
                f.getAmount(),
                f.getCreatedAt(),
                f.isPaid(),
                f.getPaidAt());
    }
}
