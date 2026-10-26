package com.temple.vazhipadu_booking.report;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class DailyCollectionController {

    private final DailyCollectionService service;

    public DailyCollectionController(
            DailyCollectionService service) {
        this.service = service;
    }

    @GetMapping("/daily-collection")
    public DailyCollectionReport getDailyCollection(
            @RequestParam Long templeId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        return service.getDailyCollection(templeId, date);
    }
}