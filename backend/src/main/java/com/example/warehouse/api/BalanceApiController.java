package com.example.warehouse.api;

import com.example.warehouse.dto.BalanceDto;
import com.example.warehouse.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/balances")
public class BalanceApiController {

    private final BalanceService balanceService;

    public BalanceApiController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public ResponseEntity<List<BalanceDto>> list(
            @RequestParam(required = false) List<Long> resourceIds,
            @RequestParam(required = false) List<Long> unitIds
    ) {
        return ResponseEntity.ok(balanceService.searchDto(resourceIds, unitIds));
    }
}