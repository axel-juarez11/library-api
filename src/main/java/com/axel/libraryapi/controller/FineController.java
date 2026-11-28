package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.FineResponse;
import com.axel.libraryapi.dto.PageResponse;
import com.axel.libraryapi.service.FineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Multas", description = "Listado de multas, búsqueda de multas por socio y pago")
@RestController
@RequiredArgsConstructor
public class FineController {

    private final FineService fineService;

    @Operation(summary = "Listar todas las multas",
    description = "Filtro opcional por estado de pago: paid=true (pagadas) o paid=false (pendientes)")
    @GetMapping("/api/fines")
    public PageResponse<FineResponse> findAll(@RequestParam(required = false) Boolean paid,
                                              @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return fineService.findAll(paid, pageable);
    }

    @Operation(summary = "Listar las multas de un socio")
    @GetMapping("/api/members/{id}/fines")
    public PageResponse<FineResponse> findByMember(@PathVariable Long id,
                                                   @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return fineService.findByMember(id, pageable);
    }

    @Operation(summary = "Pagar una multa",
    description = "Falla con 404 si no existe y con 422 si ya fue pagada")
    @PatchMapping("/api/fines/{id}/pay")
    public FineResponse pay(@PathVariable Long id) {
        return fineService.pay(id);
    }   
}
