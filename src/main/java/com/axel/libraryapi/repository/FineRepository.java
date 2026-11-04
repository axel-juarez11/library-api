package com.axel.libraryapi.repository;

import com.axel.libraryapi.entity.Fine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {
    boolean existsByLoanMemberIdAndPaidFalse(Long memberId);

    @Override
    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    List<Fine> findAll();

    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    List<Fine> findByPaid(boolean paid);

    @EntityGraph(attributePaths = {"loan", "loan.book", "loan.member"})
    List<Fine> findByLoanMemberIdOrderByCreatedAtDesc(Long memberId);
}
