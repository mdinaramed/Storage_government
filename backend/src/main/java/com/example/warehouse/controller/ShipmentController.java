package com.example.warehouse.controller;

import com.example.warehouse.dto.ShipmentDto;
import com.example.warehouse.entity.ShipmentState;
import com.example.warehouse.service.ClientService;
import com.example.warehouse.service.ResourceService;
import com.example.warehouse.service.ShipmentService;
import com.example.warehouse.service.UnitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ClientService clientService;
    private final ResourceService resourceService;
    private final UnitService unitService;

    public ShipmentController(ShipmentService shipmentService,
                              ClientService clientService,
                              ResourceService resourceService,
                              UnitService unitService) {
        this.shipmentService = shipmentService;
        this.clientService = clientService;
        this.resourceService = resourceService;
        this.unitService = unitService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) List<String> numbers,
                       @RequestParam(required = false) List<Long> resourceIds,
                       @RequestParam(required = false) List<Long> unitIds,
                       @RequestParam(required = false) ShipmentState state,
                       Model model) {

        model.addAttribute("shipments", shipmentService.search(from, to, numbers, resourceIds, unitIds, state));

        model.addAttribute("allShipmentNumbers", shipmentService.getAllNumbers());
        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());
        model.addAttribute("states", ShipmentState.values());

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("numbers", numbers);
        model.addAttribute("resourceIds", resourceIds);
        model.addAttribute("unitIds", unitIds);
        model.addAttribute("state", state);

        return "shipments/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("shipment", shipmentService.newDraft());
        model.addAttribute("clients", clientService.getAllActive());
        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());
        model.addAttribute("states", ShipmentState.values());
        return "shipments/form";
    }

    @PostMapping
    public String create(@ModelAttribute("shipment") ShipmentDto dto,
                         RedirectAttributes ra) {
        ShipmentDto created = shipmentService.create(dto);
        ra.addFlashAttribute("success", "Shipment created");
        return "redirect:/shipments/" + created.getId() + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("shipment", shipmentService.getById(id));
        model.addAttribute("clients", clientService.getAllActive());
        model.addAttribute("resources", resourceService.getAllActive());
        model.addAttribute("units", unitService.getAllActive());
        model.addAttribute("states", ShipmentState.values());
        return "shipments/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("shipment") ShipmentDto dto,
                         RedirectAttributes ra) {
        shipmentService.update(id, dto);
        ra.addFlashAttribute("success", "Shipment updated");
        return "redirect:/shipments/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        shipmentService.delete(id);
        ra.addFlashAttribute("success", "Shipment deleted");
        return "redirect:/shipments";
    }

    @PostMapping("/{id}/sign")
    public String sign(@PathVariable Long id, RedirectAttributes ra) {
        shipmentService.sign(id);
        ra.addFlashAttribute("success", "Shipment signed");
        return "redirect:/shipments/" + id + "/edit";
    }

    @PostMapping("/{id}/revoke")
    public String revoke(@PathVariable Long id, RedirectAttributes ra) {
        shipmentService.revoke(id);
        ra.addFlashAttribute("success", "Shipment revoked");
        return "redirect:/shipments/" + id + "/edit";
    }
}