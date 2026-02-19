package com.example.warehouse.controller;

import com.example.warehouse.dto.UnitDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.service.UnitService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/units")
public class UnitController {

    private final UnitService unitService;

    public UnitController(UnitService unitService) {
        this.unitService = unitService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) EntityState state,
                       Model model) {
        model.addAttribute("units", unitService.search(q, state));
        model.addAttribute("q", q);
        model.addAttribute("state", state);
        model.addAttribute("states", EntityState.values());
        return "units/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("unit", new UnitDto());
        return "units/form";
    }

    @PostMapping
    public String create(@ModelAttribute("unit") UnitDto dto,
                         RedirectAttributes ra) {
        unitService.create(dto);
        ra.addFlashAttribute("success", "Unit created");
        return "redirect:/units";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("unit", unitService.getById(id));
        return "units/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("unit") UnitDto dto,
                         RedirectAttributes ra) {
        unitService.update(id, dto);
        ra.addFlashAttribute("success", "Unit updated");
        return "redirect:/units";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes ra) {
        unitService.setState(id, EntityState.ARCHIVED);
        ra.addFlashAttribute("success", "Unit archived");
        return "redirect:/units";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        unitService.setState(id, EntityState.ACTIVE);
        ra.addFlashAttribute("success", "Unit activated");
        return "redirect:/units";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        unitService.delete(id);
        ra.addFlashAttribute("success", "Unit deleted");
        return "redirect:/units";
    }
}