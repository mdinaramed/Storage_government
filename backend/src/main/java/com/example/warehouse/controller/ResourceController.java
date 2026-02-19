package com.example.warehouse.controller;

import com.example.warehouse.dto.ResourceDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public String list(org.springframework.ui.Model model) {
        model.addAttribute("resources", resourceService.listAll());
        return "resources/list";
    }

    @GetMapping("/new")
    public String form(org.springframework.ui.Model model) {
        model.addAttribute("resourceDto", new ResourceDto());
        return "resources/form";
    }

    @PostMapping
    public String create(@ModelAttribute ResourceDto dto) {
        resourceService.create(dto);
        return "redirect:/resources";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id) {
        resourceService.setState(id, EntityState.ARCHIVED);
        return "redirect:/resources";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id) {
        resourceService.setState(id, EntityState.ACTIVE);
        return "redirect:/resources";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        resourceService.delete(id);
        return "redirect:/resources";
    }
}