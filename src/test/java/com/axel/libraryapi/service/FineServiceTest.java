package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.FineResponse;
import com.axel.libraryapi.entity.Book;
import com.axel.libraryapi.entity.Fine;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.entity.Member;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.FineRepository;
import com.axel.libraryapi.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FineServiceTest {

    @Mock private FineRepository fineRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks private FineService fineService;

    private Book book;
    private Member member;

    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(fineService, "finePerDay", new BigDecimal("5.00"));

        book = Book.builder().id(1L).title("Clean Code").totalCopies(3).availableCopies(3).build();
        member = Member.builder().id(1L).name("Ana").email("ana@mail.com").active(true).build();
    }

    private Loan overdueLoan(){
        return Loan.builder().id(10L).book(book).member(member)
                .loanDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(3))
                .returnedAt(LocalDate.now()).build();
    }

    // createFineIfOverdue
    @Test
    void createFineIfOverdue_loanOverdue_savesFineWithCorrectAmount(){
        Loan loan = overdueLoan();

        fineService.createFineIfOverdue(loan);

        ArgumentCaptor<Fine> captor = ArgumentCaptor.forClass(Fine.class);
        verify(fineRepository).save(captor.capture());
        Fine saved = captor.getValue();

        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
        assertThat(saved.isPaid()).isFalse();
        assertThat(saved.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(saved.getLoan()).isSameAs(loan);
    }

    @Test
    void createFineIfOverdue_returnedOnTime_doesNotSave(){
        Loan loan = Loan.builder().id(10L).book(book).member(member)
                .loanDate(LocalDate.now().minusDays(2))
                .dueDate(LocalDate.now().plusDays(5))
                .returnedAt(LocalDate.now()).build();

        fineService.createFineIfOverdue(loan);

        verify(fineRepository, never()).save(any());
    }

    // hasUnpaidFines
    @Test
    void hasUnpaidFines_delegatesToRepository(){
        when(fineRepository.existsByLoanMemberIdAndPaidFalse(1L)).thenReturn(true);

        assertThat(fineService.hasUnpaidFines(1L)).isTrue();
    }

    // findAll
    @Test
    void findAll_withoutFilter_returnsAll(){
        Fine fine = Fine.builder().id(5L).loan(overdueLoan())
                .amount(new BigDecimal("15.00")).paid(false)
                .createdAt(LocalDate.now()).build();
        when(fineRepository.findAll()).thenReturn(List.of(fine));

        List<FineResponse> result = fineService.findAll(null);

        assertThat(result).hasSize(1);
        verify(fineRepository, never()).findByPaid(anyBoolean());
    }

    @Test
    void findAll_withPaidFilter_usesFindByPaid(){
        when(fineRepository.findByPaid(true)).thenReturn(List.of());

        List<FineResponse> result = fineService.findAll(true);

        assertThat(result).isEmpty();
        verify(fineRepository, never()).findAll();
    }

    // findByMember
    @Test
    void findByMember_success_returnsFines(){
        Fine fine = Fine.builder().id(5L).loan(overdueLoan())
                .amount(new BigDecimal("15.00")).paid(false)
                .createdAt(LocalDate.now()).build();
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(fineRepository.findByLoanMemberIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(fine));

        List<FineResponse> result = fineService.findByMember(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByMember_memberNotFound_throwsNotFound(){
        when(memberRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> fineService.findByMember(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(fineRepository, never()).findByLoanMemberIdOrderByCreatedAtDesc(any());
    }

    // pay
    @Test
    void pay_success_marksPaid(){
        Fine fine = Fine.builder().id(5L).loan(overdueLoan())
                .amount(new BigDecimal("15.00")).paid(false)
                .createdAt(LocalDate.now()).build();
        when(fineRepository.findById(5L)).thenReturn(Optional.of(fine));

        FineResponse response = fineService.pay(5L);

        assertThat(response).isNotNull();
        assertThat(fine.isPaid()).isTrue();
        assertThat(fine.getPaidAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void pay_alreadyPaid_throwsBusinessRule(){
        Fine fine = Fine.builder().id(5L).loan(overdueLoan())
                .amount(new BigDecimal("15.00")).paid(true)
                .paidAt(LocalDate.now().minusDays(1)).build();
        when(fineRepository.findById(5L)).thenReturn(Optional.of(fine));

        assertThatThrownBy(() -> fineService.pay(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue pagada");

        assertThat(fine.getPaidAt()).isEqualTo(LocalDate.now().minusDays(1));
    }

    @Test
    void pay_notFound_throwsNotFound(){
        when(fineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fineService.pay(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
