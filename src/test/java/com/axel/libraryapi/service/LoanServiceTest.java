package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.LoanRequest;
import com.axel.libraryapi.dto.LoanResponse;
import com.axel.libraryapi.entity.Book;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.entity.LoanStatus;
import com.axel.libraryapi.entity.Member;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.BookRepository;
import com.axel.libraryapi.repository.LoanRepository;
import com.axel.libraryapi.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private FineService fineService;

    @InjectMocks private LoanService loanService;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(loanService, "loanDays", 7);
        ReflectionTestUtils.setField(loanService, "maxActiveLoans", 3);

        book = Book.builder().id(1L).title("Clean Code").totalCopies(3).availableCopies(3).build();
        member = Member.builder().id(1L).name("Ana").active(true).build();
    }

    // borrow
    @Test
    void borrow_success_createsLoanAndDecrementsCopies(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(1L)).thenReturn(0L);
        when(loanRepository.existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(any(), any()))
                .thenReturn(false);
        when(loanRepository.existsByMemberIdAndBookIdAndReturnedAtIsNull(1L, 1L)).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponse response = loanService.borrow(new LoanRequest(1L, 1L));

        assertThat(book.getAvailableCopies()).isEqualTo(2);
        assertThat(response.dueDate()).isEqualTo(LocalDate.now().plusDays(7));
        assertThat(response.status()).isEqualTo(LoanStatus.ACTIVE);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void borrow_noCopiesAvailable_throwsBusinessRule(){
        book.setAvailableCopies(0);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No hay copias");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_inactiveMember_throwsBusinessRule(){
        member.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactivo");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_memberNotFound_throwsNotFound(){
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 99L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void borrow_memberAtMaxActiveLoans_throwsBusinessRule(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(loanRepository.countByMemberIdAndReturnedAtIsNull(1L)).thenReturn(3L);

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("máximo");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_memberHasOverdueLoans_throwsBusinessRule(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("vencidos");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_bookAlreadyBorrowedByMember_throwsBusinessRule(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByMemberIdAndBookIdAndReturnedAtIsNull(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya tiene prestado");
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_bookNotFound_throwsNotFound(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(99L, 1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("no encontrado");
        
        verify(loanRepository, never()).save(any());
    }

    @Test
    void borrow_memberHasUnpaidFines_throwsBusinessRule(){
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(fineService.hasUnpaidFines(1L)).thenReturn(true);

        assertThatThrownBy(() -> loanService.borrow(new LoanRequest(1L, 1L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("multas pendientes");

        verify(loanRepository, never()).save(any());
    }

    //returnLoan
    @Test
    void returnLoan_success_incrementsCopiesAndSetsReturnDate(){
        book.setAvailableCopies(2);
        Loan loan = Loan.builder().id(10L).book(book).member(member)
                .loanDate(LocalDate.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(5)).build();
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));

        LoanResponse response = loanService.returnLoan(10L);

        assertThat(book.getAvailableCopies()).isEqualTo(3);
        assertThat(loan.getReturnedAt()).isEqualTo(LocalDate.now());
        assertThat(response.status()).isEqualTo(LoanStatus.RETURNED);
        verify(fineService).createFineIfOverdue(loan);
    }

    @Test
    void returnLoan_alreadyReturned_throwsBusinessRule(){
        Loan loan = Loan.builder().id(10L).book(book).member(member)
                .loanDate(LocalDate.now().minusDays(5))
                .dueDate(LocalDate.now().plusDays(2))
                .returnedAt(LocalDate.now().minusDays(1)).build();
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanService.returnLoan(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue devuelto");
    }
}
