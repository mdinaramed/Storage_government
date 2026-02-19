package com.example.warehouse.api;

import com.example.warehouse.dto.ClientDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.service.ClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientApiController {

    private final ClientService clientService;

    public ClientApiController(ClientService clientService) {
        this.clientService = clientService;
    }

    public record ClientCreateUpdateRequest(
            @NotBlank(message = "name is required")
            @Size(max = 200, message = "name must be <= 200 chars")
            String name,

            @Size(max = 500, message = "address must be <= 500 chars")
            String address
    ) {}

    @GetMapping
    public ResponseEntity<List<ClientDto>> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) EntityState state) {
        return ResponseEntity.ok(clientService.searchDto(q, state));
    }

    @GetMapping("/active")
    public ResponseEntity<List<ClientDto>> active() {
        return ResponseEntity.ok(clientService.getAllActiveDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClientDto> create(@Valid @RequestBody ClientCreateUpdateRequest req,
                                            UriComponentsBuilder ucb) {
        ClientDto dto = new ClientDto();
        dto.setName(req.name());
        dto.setAddress(req.address());

        ClientDto created = clientService.create(dto);

        URI location = ucb.path("/api/clients/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> update(@PathVariable Long id,
                                            @Valid @RequestBody ClientCreateUpdateRequest req) {
        ClientDto dto = new ClientDto();
        dto.setName(req.name());
        dto.setAddress(req.address());

        return ResponseEntity.ok(clientService.update(id, dto));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ClientDto> archive(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setState(id, EntityState.ARCHIVED));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ClientDto> activate(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.setState(id, EntityState.ACTIVE));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}