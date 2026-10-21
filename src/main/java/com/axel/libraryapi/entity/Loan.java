package com.axel.libraryapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, updatable = false)
    private LocalDate loanDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    private LocalDate returnedAt;

    public LoanStatus statusOn(LocalDate today){
        if (returnedAt != null) return LoanStatus.RETURNED;
        return today.isAfter(dueDate) ? LoanStatus.OVERDUE : LoanStatus.ACTIVE;
    }

    public long daysOverdueOn(LocalDate today){
        LocalDate end = (returnedAt != null) ? returnedAt : today;
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }
}
