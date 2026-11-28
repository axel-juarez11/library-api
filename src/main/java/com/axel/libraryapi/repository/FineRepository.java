package com.axel.libraryapi.repository;

import com.axel.libraryapi.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    boolean existsByLoanMemberIdAndPaidFalse(Long memberId);

    @Override
    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    Page<Fine> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    Page<Fine> findByPaid(boolean paid, Pageable pageable);

    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    Page<Fine> findByLoanMemberId(Long memberId, Pageable pageable);
}
