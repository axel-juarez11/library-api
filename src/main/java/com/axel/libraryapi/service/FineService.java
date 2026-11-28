package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.FineResponse;
import com.axel.libraryapi.dto.PageResponse;
import com.axel.libraryapi.entity.Fine;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.FineRepository;
import com.axel.libraryapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    @Value("${library.fine-per-day:5.00}")
    private BigDecimal finePerDay;

    @Transactional
    public void createFineIfOverdue(Loan loan) {
        long days = loan.daysOverdueOn(LocalDate.now(clock));
        if (days <= 0) {
            return;
        }
        Fine fine = Fine.builder()
                .loan(loan)
                .amount(finePerDay.multiply(BigDecimal.valueOf(days)))
                .createdAt(LocalDate.now(clock))
                .paid(false)
                .build();
        fineRepository.save(fine);
    }

    @Transactional(readOnly = true)
    public boolean hasUnpaidFines(Long memberId) {
        return fineRepository.existsByLoanMemberIdAndPaidFalse(memberId);
    }

    @Transactional(readOnly = true)
    public PageResponse<FineResponse> findAll(Boolean paid, Pageable pageable) {
        LocalDate today = LocalDate.now(clock);
        Page<Fine> fines = (paid == null) ? fineRepository.findAll(pageable) : fineRepository.findByPaid(paid, pageable);
        return PageResponse.from(fines.map(f -> FineResponse.from(f, today)));
    }

    @Transactional(readOnly = true)
    public PageResponse<FineResponse> findByMember(Long memberId, Pageable pageable) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Socio no encontrado con id " + memberId);
        }
        LocalDate today = LocalDate.now(clock);
        return PageResponse.from(fineRepository.findByLoanMemberId(memberId, pageable).map(f -> FineResponse.from(f, today)));
    }

    @Transactional
    public FineResponse pay(Long id) {
        LocalDate today = LocalDate.now(clock);
        Fine fine = fineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multa no encontrada con id " + id));
        if (fine.isPaid()) {
            throw new BusinessRuleException("Esta multa ya fue pagada");
        }
        fine.setPaid(true);
        fine.setPaidAt(today);
        return FineResponse.from(fine, today);
    }
}
