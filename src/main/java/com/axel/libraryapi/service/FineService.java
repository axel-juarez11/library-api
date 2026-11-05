package com.axel.libraryapi.service;

import com.axel.libraryapi.dto.FineResponse;
import com.axel.libraryapi.entity.Fine;
import com.axel.libraryapi.entity.Loan;
import com.axel.libraryapi.exception.BusinessRuleException;
import com.axel.libraryapi.exception.ResourceNotFoundException;
import com.axel.libraryapi.repository.FineRepository;
import com.axel.libraryapi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;

    @Value("${library.fine-per-day:5.00}")
    private BigDecimal finePerDay;

    @Transactional
    public void createFineIfOverdue(Loan loan) {
        long days = loan.daysOverdueOn(LocalDate.now());
        if (days <= 0) {
            return;
        }
        Fine fine = Fine.builder()
                .loan(loan)
                .amount(finePerDay.multiply(BigDecimal.valueOf(days)))
                .createdAt(LocalDate.now())
                .paid(false)
                .build();
        fineRepository.save(fine);
    }

    @Transactional(readOnly = true)
    public boolean hasUnpaidFines(Long memberId) {
        return fineRepository.existsByLoanMemberIdAndPaidFalse(memberId);
    }

    @Transactional(readOnly = true)
    public List<FineResponse> findAll(Boolean paid) {
        List<Fine> fines = (paid == null) ? fineRepository.findAll() : fineRepository.findByPaid(paid);
        return fines.stream().map(FineResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<FineResponse> findByMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Socio no encontrado con id " + memberId);
        }
        return fineRepository.findByLoanMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(FineResponse::from)
                .toList();
    }

    @Transactional
    public FineResponse pay(Long id) {
        Fine fine = fineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Multa no encontrada con id " + id));
        if (fine.isPaid()) {
            throw new BusinessRuleException("Esta multa ya fue pagada");
        }
        fine.setPaid(true);
        fine.setPaidAt(LocalDate.now());
        return FineResponse.from(fine);
    }
}
