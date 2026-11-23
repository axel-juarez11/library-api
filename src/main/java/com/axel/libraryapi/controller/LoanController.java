package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.LoanRequest;
import com.axel.libraryapi.dto.LoanResponse;
import com.axel.libraryapi.dto.PageResponse;
import com.axel.libraryapi.entity.LoanStatus;
import com.axel.libraryapi.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/api/loans")
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody LoanRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.borrow(request));
    }

    @PutMapping("/api/loans/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id){
        return loanService.returnLoan(id);
    }

    @GetMapping("/api/loans")
    public PageResponse<LoanResponse> findAll(@RequestParam(required = false) LoanStatus status,
                                              Pageable pageable){
        return loanService.findAll(status, pageable);
    }

    @GetMapping("/api/loans/{id}")
    public LoanResponse findById(@PathVariable Long id){
        return loanService.findById(id);
    }

    @GetMapping("/api/members/{id}/loans")
    public PageResponse<LoanResponse> findByMember(@PathVariable Long id, Pageable pageable){
        return loanService.findByMember(id, pageable);
    }
}