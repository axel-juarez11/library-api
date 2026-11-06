package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.FineResponse;
import com.axel.libraryapi.service.FineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @GetMapping("/api/fines")
    public List<FineResponse> findAll(@RequestParam(required = false) Boolean paid) {
        return fineService.findAll(paid);
    }

    @GetMapping("/api/members/{id}/fines")
    public List<FineResponse> findByMember(@PathVariable Long id) {
        return fineService.findByMember(id);
    }

    @PatchMapping("/api/fines/{id}/pay")
    public FineResponse pay(@PathVariable Long id) {
        return fineService.pay(id);
    }   
}
