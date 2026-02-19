package com.example.warehouse.controller;

import com.example.warehouse.service.BalanceService;
import com.example.warehouse.service.ResourceService;
import com.example.warehouse.service.UnitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/balances")
public class BalanceController {

    private final BalanceService balanceService;
    private final ResourceService resourceService;
    private final UnitService unitService;

    public BalanceController(BalanceService balanceService,
                             ResourceService resourceService,
                             UnitService unitService) {
        this.balanceService = balanceService;
        this.resourceService = resourceService;
        this.unitService = unitService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) List<Long> resourceIds,
                       @RequestParam(required = false) List<Long> unitIds,
                       Model model) {

        model.addAttribute("balances", balanceService.search(resourceIds, unitIds));

        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());

        model.addAttribute("resourceIds", resourceIds);
        model.addAttribute("unitIds", unitIds);

        return "balances/list";
    }
}