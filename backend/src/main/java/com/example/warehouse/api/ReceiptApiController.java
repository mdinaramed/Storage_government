package com.example.warehouse.api;

import com.example.warehouse.dto.ReceiptDto;
import com.example.warehouse.dto.ReceiptListItemDto;
import com.example.warehouse.service.ReceiptService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptApiController {

    private final ReceiptService receiptService;

    public ReceiptApiController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping
    public List<ReceiptListItemDto> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<String> numbers,
            @RequestParam(required = false) List<Long> resourceIds,
            @RequestParam(required = false) List<Long> unitIds
    ) {
        return receiptService.searchListDto(from, to, numbers, resourceIds, unitIds);
    }

    @GetMapping("/{id}")
    public ReceiptDto get(@PathVariable Long id) {
        return receiptService.getById(id);
    }

    @PostMapping
    public ReceiptDto create(@Valid @RequestBody ReceiptDto dto) {
        return receiptService.create(dto);
    }

    @PutMapping("/{id}")
    public ReceiptDto update(@PathVariable Long id, @Valid @RequestBody ReceiptDto dto) {
        return receiptService.updateAndReturn(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        receiptService.delete(id);
    }
}