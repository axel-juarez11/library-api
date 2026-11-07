package com.axel.libraryapi.controller;

import com.axel.libraryapi.dto.MostBorrowedBookResponse;
import com.axel.libraryapi.dto.OverdueReportItem;
import com.axel.libraryapi.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/most-borrowed")
    public List<MostBorrowedBookResponse> mostBorrowed(@RequestParam(defaultValue = "5") int limit) {
        return reportService.mostBorrowed(limit);
    }

    @GetMapping("/overdue")
    public List<OverdueReportItem> overdue() {
        return reportService.overdue();
    }
}
