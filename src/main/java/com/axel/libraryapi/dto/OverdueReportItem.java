package com.axel.libraryapi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OverdueReportItem(
        Long loanId,
        String memberName,
        String memberEmail,
        String bookTitle,
        LocalDate dueDate,
        long daysOverdue,
        BigDecimal estimatedFine
) {}
