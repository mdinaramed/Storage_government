package com.example.warehouse.service;

import com.example.warehouse.dto.ClientDto;
import com.example.warehouse.entity.Client;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.exception.BusinessException;
import com.example.warehouse.exception.NotFoundException;
import com.example.warehouse.repository.ClientRepository;
import com.example.warehouse.util.Normalize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<Client> search(String q, EntityState state) {
        String normalizedQ = Normalize.normalize(q);

        if ((normalizedQ == null || normalizedQ.isBlank()) && state == null) {
            return clientRepository.findAll();
        }
        if (normalizedQ == null || normalizedQ.isBlank()) {
            return clientRepository.findByState(state);
        }
        if (state == null) {
            return clientRepository.findByNameContainingIgnoreCase(normalizedQ);
        }
        return clientRepository.findByNameContainingIgnoreCaseAndState(normalizedQ, state);
    }

    @Transactional(readOnly = true)
    public List<Client> getAllActive() {
        return clientRepository.findByState(EntityState.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<ClientDto> searchDto(String q, EntityState state) {
        return search(q, state).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ClientDto> getAllActiveDto() {
        return getAllActive().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ClientDto getById(Long id) {
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        return toDto(c);
    }

    @Transactional
    public ClientDto create(ClientDto dto) {
        if (dto == null) throw new BusinessException("Client is required");

        String name = Normalize.normalize(dto.getName());
        if (name == null || name.isBlank()) throw new BusinessException("Client name is required");

        if (clientRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Client with this name already exists");
        }

        Client c = new Client();
        c.setName(name);

        String address = Normalize.normalize(dto.getAddress());
        c.setAddress((address == null || address.isBlank()) ? null : address);

        c.setState(EntityState.ACTIVE);

        Client saved = clientRepository.save(c);
        return toDto(saved);
    }

    @Transactional
    public ClientDto update(Long id, ClientDto dto) {
        if (dto == null) throw new BusinessException("Client is required");

        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        String name = Normalize.normalize(dto.getName());
        if (name == null || name.isBlank()) throw new BusinessException("Client name is required");

        if (!c.getName().equalsIgnoreCase(name) && clientRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Client with this name already exists");
        }

        c.setName(name);

        String address = Normalize.normalize(dto.getAddress());
        c.setAddress((address == null || address.isBlank()) ? null : address);

        Client saved = clientRepository.save(c);
        return toDto(saved);
    }

    @Transactional
    public ClientDto setState(Long id, EntityState state) {
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        c.setState(state);
        Client saved = clientRepository.save(c);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id) {
        Client c = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        clientRepository.delete(c);
    }

    private ClientDto toDto(Client c) {
        ClientDto dto = new ClientDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setAddress(c.getAddress());
        dto.setState(c.getState());
        return dto;
    }
}