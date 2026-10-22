package com.axel.libraryapi.dto;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.entity.LoanStatus;
import java.time.LocalDate;

public record LoanResponse(
        Long id, Long bookId, String bookTitle, Long memberId, String memberName,
        LocalDate loanDate, LocalDate dueDate, LocalDate returnedAt, LoanStatus status,
        long daysOverDue
) {
    public static LoanResponse from(Loan l, LocalDate today){
        return new LoanResponse(l.getId(), l.getBook().getId(), l.getBook().getTitle(),
                l.getMember().getId(), l.getMember().getName(), l.getLoanDate(),
                l.getDueDate(), l.getReturnedAt(), l.statusOn(today), l.daysOverdueOn(today));
    }
}
