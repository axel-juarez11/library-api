package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.MostBorrowedBookResponse;
import com.axel.libraryapi.dto.OverdueReportItem;
import com.axel.libraryapi.entity.Book;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoanRepository loanRepository;
    private final Clock clock;

    @Value("${library.fine-per-day:5.00}")
    private BigDecimal finePerDay;

    @Transactional(readOnly = true)
    public List<MostBorrowedBookResponse> mostBorrowed(int limit) {
        Map<Long, List<Loan>> loansByBook = loanRepository.findAll().stream()
                .collect(Collectors.groupingBy(loan -> loan.getBook().getId()));

        return loansByBook.values().stream()
                .map(loans -> {
                    Book book = loans.get(0).getBook();
                    return new MostBorrowedBookResponse(
                            book.getId(), book.getTitle(), book.getAuthor(), loans.size());
                })
                .sorted(Comparator.comparingLong(MostBorrowedBookResponse::timesBorrowed).reversed()
                        .thenComparing(MostBorrowedBookResponse::title))
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OverdueReportItem> overdue() {
        LocalDate today = LocalDate.now(clock);

        return loanRepository.findByReturnedAtIsNullAndDueDateBefore(today).stream()
                .map(loan -> {
                    long days = loan.daysOverdueOn(today);
                    return new OverdueReportItem(
                            loan.getId(),
                            loan.getMember().getName(),
                            loan.getMember().getEmail(),
                            loan.getBook().getTitle(),
                            loan.getDueDate(),
                            days,
                            finePerDay.multiply(BigDecimal.valueOf(days)));
                })
                .sorted(Comparator.comparingLong(OverdueReportItem::daysOverdue).reversed())
                .toList();
    }
}
