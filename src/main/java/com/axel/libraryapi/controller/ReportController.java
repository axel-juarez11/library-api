package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.MostBorrowedBookResponse;
import com.axel.libraryapi.dto.OverdueReportItem;
import com.axel.libraryapi.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Reportes", description = "Reportes sobre los libros más prestados y préstamos vencidos")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Listar libros más prestados",
    description = "Ranking por número de préstamos, de mayor a menor. 'limit' indica cuántos libros devolver (por defecto 5).")
    @GetMapping("/most-borrowed")
    public List<MostBorrowedBookResponse> mostBorrowed(@RequestParam(defaultValue = "5") int limit) {
        return reportService.mostBorrowed(limit);
    }

    @Operation(summary = "Listar préstamos vencidos",
    description = "Préstamos sin devolver con la fecha límite pasada, con días de retraso y multa estimada. Ordenados por más días de retraso.")
    @GetMapping("/overdue")
    public List<OverdueReportItem> overdue() {
        return reportService.overdue();
    }
}
