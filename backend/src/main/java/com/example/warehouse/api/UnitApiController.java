package com.example.warehouse.api;

import com.example.warehouse.dto.UnitDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.service.UnitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/units")
public class UnitApiController {

    private final UnitService unitService;

    public UnitApiController(UnitService unitService) {
        this.unitService = unitService;
    }

    public record UnitCreateUpdateRequest(
            @NotBlank(message = "name is required") String name
    ) {}

    @GetMapping
    public ResponseEntity<List<UnitDto>> list(@RequestParam(required = false) String q,
                                              @RequestParam(required = false) EntityState state) {
        return ResponseEntity.ok(unitService.searchDto(q, state));
    }

    @GetMapping("/active")
    public ResponseEntity<List<UnitDto>> active() {
        return ResponseEntity.ok(unitService.getAllActiveDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnitDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getById(id));
    }

    @PostMapping
    public ResponseEntity<UnitDto> create(@Valid @RequestBody UnitCreateUpdateRequest req,
                                          UriComponentsBuilder ucb) {
        UnitDto dto = new UnitDto();
        dto.setName(req.name());

        UnitDto created = unitService.create(dto);

        URI location = ucb.path("/api/units/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitDto> update(@PathVariable Long id,
                                          @Valid @RequestBody UnitCreateUpdateRequest req) {
        UnitDto dto = new UnitDto();
        dto.setName(req.name());

        return ResponseEntity.ok(unitService.update(id, dto));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<UnitDto> archive(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.setState(id, EntityState.ARCHIVED));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<UnitDto> activate(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.setState(id, EntityState.ACTIVE));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}