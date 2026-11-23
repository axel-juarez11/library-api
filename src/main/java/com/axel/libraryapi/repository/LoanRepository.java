package com.axel.libraryapi.repository;

import com.axel.libraryapi.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long>{

    long countByMemberIdAndReturnedAtIsNull(Long memberId);

    boolean existsByMemberIdAndReturnedAtIsNullAndDueDateBefore(Long memberId, LocalDate today);

    boolean existsByMemberIdAndBookIdAndReturnedAtIsNull(Long memberId, Long bookId);

    boolean existsByBookId(Long bookId);

    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findAll(Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findAll();

    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findByMemberIdOrderByLoanDateDesc(Long memberId, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findByReturnedAtIsNotNull(Pageable pageable);

    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findByReturnedAtIsNullAndDueDateBefore(LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findByReturnedAtIsNullAndDueDateBefore(LocalDate today);

    @EntityGraph(attributePaths = {"book", "member"})
    Page<Loan> findByReturnedAtIsNullAndDueDateGreaterThanEqual(LocalDate today, Pageable pageable);
}
