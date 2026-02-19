package com.example.warehouse.controller;

import com.example.warehouse.dto.ReceiptDto;
import com.example.warehouse.service.ReceiptService;
import com.example.warehouse.service.ResourceService;
import com.example.warehouse.service.UnitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final ResourceService resourceService;
    private final UnitService unitService;

    public ReceiptController(ReceiptService receiptService,
                             ResourceService resourceService,
                             UnitService unitService) {
        this.receiptService = receiptService;
        this.resourceService = resourceService;
        this.unitService = unitService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) List<String> numbers,
                       @RequestParam(required = false) List<Long> resourceIds,
                       @RequestParam(required = false) List<Long> unitIds,
                       Model model) {

        model.addAttribute("receipts", receiptService.search(from, to, numbers, resourceIds, unitIds));

        model.addAttribute("allReceiptNumbers", receiptService.getAllNumbers());
        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("numbers", numbers);
        model.addAttribute("resourceIds", resourceIds);
        model.addAttribute("unitIds", unitIds);

        return "receipts/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("receipt", receiptService.newDraft());
        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());
        return "receipts/form";
    }

    @PostMapping
    public String create(@ModelAttribute("receipt") ReceiptDto dto,
                         RedirectAttributes ra) {
        ReceiptDto created = receiptService.create(dto);
        ra.addFlashAttribute("success", "Receipt created");
        return "redirect:/receipts/" + created.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("receipt", receiptService.getById(id));
        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());
        return "receipts/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("receipt") ReceiptDto dto,
                         RedirectAttributes ra) {
        receiptService.update(id, dto);
        ra.addFlashAttribute("success", "Receipt updated");
        return "redirect:/receipts/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        receiptService.delete(id);
        ra.addFlashAttribute("success", "Receipt deleted");
        return "redirect:/receipts";
    }
}