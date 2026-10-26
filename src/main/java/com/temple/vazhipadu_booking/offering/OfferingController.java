package com.temple.vazhipadu_booking.offering;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offerings")
public class OfferingController {

    private final OfferingRepository repository;

    public OfferingController(OfferingRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Offering create(@Valid @RequestBody CreateOfferingRequest request) {
        String name = request.name().trim();

        if (repository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                "An offering with this name already exists");
        }

        return repository.save(
            new Offering(name, request.price(), request.active(),request.dailyLimit()));
    }

    @GetMapping
    public List<Offering> list() {
        return repository.findAll();
    }
}
