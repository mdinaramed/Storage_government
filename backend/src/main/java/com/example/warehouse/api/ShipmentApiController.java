package com.example.warehouse.api;

import com.example.warehouse.dto.ShipmentDto;
import com.example.warehouse.entity.Shipment;
import com.example.warehouse.entity.ShipmentState;
import com.example.warehouse.service.ShipmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentApiController {

    private final ShipmentService shipmentService;

    public ShipmentApiController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping
    public List<ShipmentDto> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) List<String> numbers,
            @RequestParam(required = false) List<Long> resourceIds,
            @RequestParam(required = false) List<Long> unitIds,
            @RequestParam(required = false) ShipmentState state
    ) {
        List<Shipment> found = shipmentService.search(from, to, numbers, resourceIds, unitIds, state);

        List<ShipmentDto> result = new ArrayList<>();
        for (Shipment s : found) {
            result.add(shipmentService.getById(s.getId()));
        }
        return result;
    }

    @GetMapping("/{id}")
    public ShipmentDto get(@PathVariable Long id) {
        return shipmentService.getById(id);
    }

    @PostMapping
    public ShipmentDto create(@RequestBody ShipmentDto dto) {
        return shipmentService.create(dto);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ShipmentDto dto) {
        shipmentService.update(id, dto);
    }

    @PostMapping("/{id}/sign")
    public void sign(@PathVariable Long id) {
        shipmentService.sign(id);
    }

    @PostMapping("/{id}/revoke")
    public void revoke(@PathVariable Long id) {
        shipmentService.revoke(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        shipmentService.delete(id);
    }
}