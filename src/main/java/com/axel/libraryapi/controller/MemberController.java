package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.MemberRequest;
import com.axel.libraryapi.dto.MemberResponse;
import com.axel.libraryapi.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @GetMapping
    public List<MemberResponse> findAll(@RequestParam(required = false) Boolean active){
        return memberService.findAll(active);
    }

    @GetMapping("/{id}")
    public MemberResponse findById(@PathVariable Long id){
        return memberService.findById(id);
    }

    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request){
        return memberService.update(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public MemberResponse deactivate(@PathVariable Long id){
        return memberService.setActive(id, false);
    }

    @PatchMapping("/{id}/activate")
    public MemberResponse activate(@PathVariable Long id){
        return memberService.setActive(id, true);
    }
}
