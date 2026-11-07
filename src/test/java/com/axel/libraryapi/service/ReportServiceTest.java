package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.MostBorrowedBookResponse;
import com.axel.libraryapi.dto.OverdueReportItem;
import com.axel.libraryapi.entity.Book;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.entity.Member;
import com.axel.libraryapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {
    @Mock private LoanRepository loanRepository;

    @InjectMocks private ReportService reportService;

    private Book cleanCode;
    private Book effectiveJava;
    private Member member;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(reportService, "finePerDay", new BigDecimal("5.00"));

        cleanCode = Book.builder().id(1L).title("Clean Code").author("Robert C. Martin").build();
        effectiveJava = Book.builder().id(2L).title("Effective Java").author("Joshua Bloch").build();
        member = Member.builder().id(1L).name("Ana").email("ana@mail.com").build();
    }

    private Loan loan(Long id, Book book, LocalDate dueDate){
        return Loan.builder().id(id).book(book).member(member)
                .loanDate(dueDate.minusDays(7))
                .dueDate(dueDate).build();
    }

    // mostBorrowed
    @Test
    void mostBorrowed_ranksByTimesBorrowedDescending(){
        LocalDate due = LocalDate.now().plusDays(3);
        when(loanRepository.findAll()).thenReturn(List.of(
                loan(1L, cleanCode, due),
                loan(2L, effectiveJava, due),
                loan(3L, cleanCode, due)));

        List<MostBorrowedBookResponse> result = reportService.mostBorrowed(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
        assertThat(result.get(0).timesBorrowed()).isEqualTo(2);
        assertThat(result.get(1).title()).isEqualTo("Effective Java");
        assertThat(result.get(1).timesBorrowed()).isEqualTo(1);
    }

    @Test
    void mostBorrowed_respectsLimit(){
        LocalDate due = LocalDate.now().plusDays(3);
        when(loanRepository.findAll()).thenReturn(List.of(
                loan(1L, cleanCode, due),
                loan(2L, effectiveJava, due),
                loan(3L, cleanCode, due)));

        List<MostBorrowedBookResponse> result = reportService.mostBorrowed(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Clean Code");
    }

    @Test
    void mostBorrowed_tieBreaksByTitle(){
        Book zebra = Book.builder().id(1L).title("Zebra").author("A").build();
        Book alpha = Book.builder().id(2L).title("Alpha").author("B").build();
        LocalDate due = LocalDate.now().plusDays(3);
        when(loanRepository.findAll()).thenReturn(List.of(
                loan(1L, zebra, due),
                loan(2L, alpha, due)));

        List<MostBorrowedBookResponse> result = reportService.mostBorrowed(10);
        
        assertThat(result.get(0).title()).isEqualTo("Alpha");
        assertThat(result.get(1).title()).isEqualTo("Zebra");
    }

    // overdue
    @Test
    void overdue_calculatesDaysAndFine(){
        LocalDate today = LocalDate.now();
        when(loanRepository.findByReturnedAtIsNullAndDueDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(loan(1L, cleanCode, today.minusDays(3))));

        List<OverdueReportItem> result = reportService.overdue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).daysOverdue()).isEqualTo(3);
        assertThat(result.get(0).estimatedFine()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void overdue_sortsByDaysOverdueDescending(){
        LocalDate today = LocalDate.now();
        when(loanRepository.findByReturnedAtIsNullAndDueDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(
                        loan(1L, cleanCode, today.minusDays(2)),
                        loan(2L, effectiveJava, today.minusDays(9))));

        List<OverdueReportItem> result = reportService.overdue();

        assertThat(result.get(0).daysOverdue()).isEqualTo(9);
        assertThat(result.get(1).daysOverdue()).isEqualTo(2);
    }
}
