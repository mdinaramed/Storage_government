package com.example.warehouse.api;

import com.example.warehouse.dto.ResourceDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.service.ResourceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceApiController {

    private final ResourceService resourceService;

    public ResourceApiController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public record ResourceCreateUpdateRequest(
            @NotBlank(message = "name is required") String name
    ) {}

    @GetMapping
    public ResponseEntity<List<ResourceDto>> list() {
        return ResponseEntity.ok(resourceService.listAllDto());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ResourceDto>> active() {
        return ResponseEntity.ok(resourceService.getAllActiveDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ResourceDto> create(@Valid @RequestBody ResourceCreateUpdateRequest req,
                                              UriComponentsBuilder ucb) {
        ResourceDto dto = new ResourceDto();
        dto.setName(req.name());

        ResourceDto created = resourceService.create(dto);

        URI location = ucb.path("/api/resources/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> update(@PathVariable Long id,
                                              @Valid @RequestBody ResourceCreateUpdateRequest req) {
        ResourceDto dto = new ResourceDto();
        dto.setName(req.name());

        return ResponseEntity.ok(resourceService.update(id, dto));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ResourceDto> archive(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.setState(id, EntityState.ARCHIVED));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ResourceDto> activate(@PathVariable Long id) {
        return ResponseEntity.ok(resourceService.setState(id, EntityState.ACTIVE));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}