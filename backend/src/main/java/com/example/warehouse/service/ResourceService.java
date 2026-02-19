package com.example.warehouse.service;

import com.example.warehouse.dto.ResourceDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.entity.Resource;
import com.example.warehouse.exception.BusinessException;
import com.example.warehouse.exception.NotFoundException;
import com.example.warehouse.repository.ReceiptItemRepository;
import com.example.warehouse.repository.ResourceRepository;
import com.example.warehouse.repository.ShipmentItemRepository;
import com.example.warehouse.util.Normalize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ShipmentItemRepository shipmentItemRepository;

    public ResourceService(ResourceRepository resourceRepository,
                           ReceiptItemRepository receiptItemRepository,
                           ShipmentItemRepository shipmentItemRepository) {
        this.resourceRepository = resourceRepository;
        this.receiptItemRepository = receiptItemRepository;
        this.shipmentItemRepository = shipmentItemRepository;
    }

    @Transactional(readOnly = true)
    public List<Resource> listAll() {
        return resourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Resource> getAllActive() {
        return resourceRepository.findAllByState(EntityState.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> listAllDto() {
        return listAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> getAllActiveDto() {
        return getAllActive().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ResourceDto getById(Long id) {
        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
        return toDto(r);
    }

    @Transactional
    public ResourceDto create(ResourceDto dto) {
        if (dto == null) throw new BusinessException("Resource is required");

        String name = Normalize.normalize(dto.getName());
        if (name == null || name.isBlank()) throw new BusinessException("Name is required");

        if (resourceRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Resource with this name already exists");
        }

        Resource r = new Resource();
        r.setName(name);
        r.setState(EntityState.ACTIVE);

        return toDto(resourceRepository.save(r));
    }

    @Transactional
    public ResourceDto update(Long id, ResourceDto dto) {
        if (dto == null) throw new BusinessException("Resource is required");

        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        String name = Normalize.normalize(dto.getName());
        if (name == null || name.isBlank()) throw new BusinessException("Name is required");

        if (!r.getName().equalsIgnoreCase(name) && resourceRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Resource with this name already exists");
        }

        r.setName(name);
        return toDto(resourceRepository.save(r));
    }

    @Transactional
    public ResourceDto setState(Long id, EntityState state) {
        Resource r = resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));
        r.setState(state);
        return toDto(resourceRepository.save(r));
    }

    @Transactional
    public void delete(Long id) {
        if (receiptItemRepository.existsByResourceId(id) || shipmentItemRepository.existsByResourceId(id)) {
            throw new BusinessException("Resource is used. Archive it instead.");
        }
        resourceRepository.deleteById(id);
    }

    private ResourceDto toDto(Resource r) {
        ResourceDto dto = new ResourceDto();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setState(r.getState());
        return dto;
    }
}