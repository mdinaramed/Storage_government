package com.example.warehouse.controller;

import com.example.warehouse.dto.ClientDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.service.ClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) EntityState state,
                       Model model) {
        model.addAttribute("clients", clientService.search(q, state));
        model.addAttribute("q", q);
        model.addAttribute("state", state);
        model.addAttribute("states", EntityState.values());
        return "clients/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("client", new ClientDto());
        return "clients/form";
    }

    @PostMapping
    public String create(@ModelAttribute("client") ClientDto dto,
                         RedirectAttributes ra) {
        clientService.create(dto);
        ra.addFlashAttribute("success", "Client created");
        return "redirect:/clients";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getById(id));
        return "clients/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("client") ClientDto dto,
                         RedirectAttributes ra) {
        clientService.update(id, dto);
        ra.addFlashAttribute("success", "Client updated");
        return "redirect:/clients";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes ra) {
        clientService.setState(id, EntityState.ARCHIVED);
        ra.addFlashAttribute("success", "Client archived");
        return "redirect:/clients";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes ra) {
        clientService.setState(id, EntityState.ACTIVE);
        ra.addFlashAttribute("success", "Client activated");
        return "redirect:/clients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        clientService.delete(id);
        ra.addFlashAttribute("success", "Client deleted");
        return "redirect:/clients";
    }
}