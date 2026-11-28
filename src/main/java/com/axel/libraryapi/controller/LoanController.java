package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.LoanRequest;
import com.axel.libraryapi.dto.LoanResponse;
import com.axel.libraryapi.dto.PageResponse;
import com.axel.libraryapi.entity.LoanStatus;
import com.axel.libraryapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Préstamos", description = "Préstamos y devoluciones")
@RestController
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @Operation(summary = "Prestar un libro")
    @PostMapping("/api/loans")
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody LoanRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(loanService.borrow(request));
    }

    @Operation(summary = "Devolver un préstamo")
    @PutMapping("/api/loans/{id}/return")
    public LoanResponse returnLoan(@PathVariable Long id){
        return loanService.returnLoan(id);
    }

    @Operation(summary = "Listar préstamos, con filtro especial por estado")
    @GetMapping("/api/loans")
    public PageResponse<LoanResponse> findAll(@RequestParam(required = false) LoanStatus status,
                                              Pageable pageable){
        return loanService.findAll(status, pageable);
    }

    @Operation(summary = "Obtener un préstamo por id")
    @GetMapping("/api/loans/{id}")
    public LoanResponse findById(@PathVariable Long id){
        return loanService.findById(id);
    }

    @Operation(summary = "Listar el historial de préstamos de un socio")
    @GetMapping("/api/members/{id}/loans")
    public PageResponse<LoanResponse> findByMember(@PathVariable Long id, Pageable pageable){
        return loanService.findByMember(id, pageable);
    }
}