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
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    @Value("{library.loan-days:7}")
    private int loanDays;

    @Value("{library.max-active-loans:3}")
    private int maxActiveLoans;

    @Transactional
    public LoanResponse borrow(LoanRequest request){
        LocalDate today = LocalDate.now();

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
        LocalDate today = LocalDate.now();

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

        return LoanResponse.from(loan, today);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findById(Long id){
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestamo no encontrado con id " + id));
        return LoanResponse.from(loan, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findAll(LoanStatus status){
        LocalDate today = LocalDate.now();
        List<Loan> loans;
        if (status == null){
            loans = loanRepository.findAll();
        } else {
             loans = switch (status) {
                case ACTIVE -> loanRepository.findByReturnedAtIsNullAndDueDateGreaterThanEqual(today);
                case OVERDUE -> loanRepository.findByReturnedAtIsNullAndDueDateBefore(today);
                case RETURNED -> loanRepository.findByReturnedAtIsNotNull();
            };
        }
        return loans.stream().map(l -> LoanResponse.from(l, today)).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> findByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Socio no encontrado con id " + memberId);
        }
        LocalDate today = LocalDate.now();
        return loanRepository.findByMemberIdOrderByLoanDateDesc(memberId).stream()
                .map(l -> LoanResponse.from(l, today))
                .toList();
    }
}
