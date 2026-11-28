package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.MemberRequest;
import com.axel.libraryapi.dto.MemberResponse;
import com.axel.libraryapi.dto.PageResponse;
import com.axel.libraryapi.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Socios", description = "Creación, listado, actualización, activación y desactivación de socios")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "Crear un socio")
    @PostMapping
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.create(request));
    }

    @Operation(summary = "Listar socios",
    description = "Filtro opcional por estado: active=true (activos) o active=false (inactivos)")
    @GetMapping
    public PageResponse<MemberResponse> findAll(@RequestParam(required = false) Boolean active,
                                                @PageableDefault(sort = "name") Pageable pageable){
        return memberService.findAll(active, pageable);
    }

    @Operation(summary = "Obtener un socio por id")
    @GetMapping("/{id}")
    public MemberResponse findById(@PathVariable Long id){
        return memberService.findById(id);
    }

    @Operation(summary = "Actualizar un socio")
    @PutMapping("/{id}")
    public MemberResponse update(@PathVariable Long id, @Valid @RequestBody MemberRequest request){
        return memberService.update(id, request);
    }

    @Operation(summary = "Desactivar un socio",
    description = "Falla con 422 si el socio tiene préstamos activos")
    @PatchMapping("/{id}/deactivate")
    public MemberResponse deactivate(@PathVariable Long id){
        return memberService.setActive(id, false);
    }

    @Operation(summary = "Activar un socio")
    @PatchMapping("/{id}/activate")
    public MemberResponse activate(@PathVariable Long id){
        return memberService.setActive(id, true);
    }
}
