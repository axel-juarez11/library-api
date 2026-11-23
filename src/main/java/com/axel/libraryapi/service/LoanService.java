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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import com.axel.libraryapi.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Value("${library.loan-days:7}")
    private int loanDays;

    @Value("${library.max-active-loans:3}")
    private int maxActiveLoans;

    private final FineService fineService;
    private final Clock clock;

    @Transactional
    public LoanResponse borrow(LoanRequest request){
        LocalDate today = LocalDate.now(clock);

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Socio no encontrado con id " + request.memberId()));

        Book book = bookRepository.findByIdForUpdate(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Libro no encontrado con id " + request.bookId()));

        if (!member.isActive()){
            throw new BusinessRuleException("El socio está inactivo");
        }

        if (book.getAvailableCopies() <= 0){
            throw new BusinessRuleException("No hay copias disponibles de '" + book.getTitle() + "'");
        }

        if (loanRepository.countByMemberIdAndReturnedAtIsNull(member.getId()) >= maxActiveLoans) {
            throw new BusinessRuleException("El socio ya tiene el máximo de " + maxActiveLoans + " préstamos activos");
        }

        if (loanRepository.existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(member.getId(), today)){
            throw new BusinessRuleException("El socio tiene préstamos vencidos sin devolver");
        }

        if (fineService.hasUnpaidFines(member.getId())) {
            throw new BusinessRuleException("El socio tiene multas pendientes de pago");
        }

        if (loanRepository.existsByMemberIdAndBookIdAndReturnedAtIsNull(member.getId(), book.getId())){
            throw new BusinessRuleException("El socio ya tiene prestado este libro");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);

        Loan loan = Loan.builder()
                .book(book)
                .member(member)
                .loanDate(today)
                .dueDate(today.plusDays(loanDays))
                .build();

        return LoanResponse.from(loanRepository.save(loan), today);
    }

    @Transactional
    public LoanResponse returnLoan(Long loanId){
        LocalDate today = LocalDate.now(clock);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prestamo no encontrado con id " + loanId));

        if (loan.getReturnedAt() != null){
            throw new BusinessRuleException("Este préstamo ya fue devuelto");
        }

        Book book = bookRepository.findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        loan.setReturnedAt(today);
        fineService.createFineIfOverdue(loan);

        return LoanResponse.from(loan, today);
    }

    @Transactional(readOnly = true)
    public LoanResponse findById(Long id){
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado con id " + id));
        return LoanResponse.from(loan, LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> findAll(LoanStatus status, Pageable pageable){
        LocalDate today = LocalDate.now(clock);
        Page<Loan> loans;
        if (status == null){
            loans = loanRepository.findAll(pageable);
        } else {
            loans = switch (status) {
                case ACTIVE -> loanRepository.findByReturnedAtIsNullAndDueDateGreaterThanEqual(today, pageable);
                case OVERDUE -> loanRepository.findByReturnedAtIsNullAndDueDateBefore(today, pageable);
                case RETURNED -> loanRepository.findByReturnedAtIsNotNull(pageable);
            };
        }
        return PageResponse.from(loans.map(l -> LoanResponse.from(l, today)));
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> findByMember(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Socio no encontrado con id " + memberId);
        }
        LocalDate today = LocalDate.now(clock);
        return PageResponse.from(
                loanRepository.findByMemberIdOrderByLoanDateDesc(memberId, pageable)
                        .map(l -> LoanResponse.from(l, today)));
    }
}
