package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.LoanRequest;
import com.axel.libraryapi.dto.LoanResponse;
import com.axel.libraryapi.entity.LoanStatus;
import com.axel.libraryapi.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public List<LoanResponse> findAll(@RequestParam(required = false) LoanStatus status){
        return loanService.findAll(status);
    }

    @GetMapping("/api/loans/{id}")
    public LoanResponse findById(@PathVariable Long id){
        return loanService.findById(id);
    }

    @GetMapping("/api/members/{id}/loans")
    public List<LoanResponse> findByMember(@PathVariable Long id){
        return loanService.findByMember(id);
    }
}
