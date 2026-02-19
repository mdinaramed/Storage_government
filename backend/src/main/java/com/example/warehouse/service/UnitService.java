package com.example.warehouse.service;

import com.example.warehouse.dto.UnitDto;
import com.example.warehouse.entity.EntityState;
import com.example.warehouse.entity.Unit;
import com.example.warehouse.exception.BusinessException;
import com.example.warehouse.exception.NotFoundException;
import com.example.warehouse.repository.ReceiptItemRepository;
import com.example.warehouse.repository.ShipmentItemRepository;
import com.example.warehouse.repository.UnitRepository;
import com.example.warehouse.util.Normalize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UnitService {

    private final UnitRepository unitRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ShipmentItemRepository shipmentItemRepository;

    public UnitService(UnitRepository unitRepository,
                       ReceiptItemRepository receiptItemRepository,
                       ShipmentItemRepository shipmentItemRepository) {
        this.unitRepository = unitRepository;
        this.receiptItemRepository = receiptItemRepository;
        this.shipmentItemRepository = shipmentItemRepository;
    }

    @Transactional(readOnly = true)
    public List<Unit> search(String q, EntityState state) {
        String normalizedQ = Normalize.normalize(q);

        if ((normalizedQ == null || normalizedQ.isBlank()) && state == null) {
            return unitRepository.findAll();
        }
        if (normalizedQ == null || normalizedQ.isBlank()) {
            return unitRepository.findAllByState(state);
        }

        List<Unit> base = (state == null) ? unitRepository.findAll() : unitRepository.findAllByState(state);
        String qLower = normalizedQ.toLowerCase();

        return base.stream()
                .filter(u -> u.getName() != null && u.getName().toLowerCase().contains(qLower))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Unit> getAllActive() {
        return unitRepository.findAllByState(EntityState.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<UnitDto> searchDto(String q, EntityState state) {
        return search(q, state).stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UnitDto> getAllActiveDto() {
        return getAllActive().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UnitDto getById(Long id) {
        Unit u = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        return toDto(u);
    }

    @Transactional
    public UnitDto create(UnitDto dto) {
        if (dto == null) throw new BusinessException("Unit is required");

        String name = Normalize.normalize(dto.getName());
        if (name == null || name.isBlank()) throw new BusinessException("Unit name is required");

        if (unitRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Unit with this name already exists");
        }

        Unit u = new Unit();
        u.setName(name);
        u.setState(EntityState.ACTIVE);

        return toDto(unitRepository.save(u));
    }

    @Transactional
    public UnitDto update(Long id, UnitDto dto) {
        if (dto == null) throw new BusinessException("Unit is required");

        Unit u = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));

        String name = Normalize.normalize(dto.getName());
        if (name == null || name.isBlank()) throw new BusinessException("Unit name is required");

        if (!u.getName().equalsIgnoreCase(name) && unitRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException("Unit with this name already exists");
        }

        u.setName(name);
        return toDto(unitRepository.save(u));
    }

    @Transactional
    public UnitDto setState(Long id, EntityState state) {
        Unit u = unitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Unit not found"));
        u.setState(state);
        return toDto(unitRepository.save(u));
    }

    @Transactional
    public void delete(Long id) {
        if (receiptItemRepository.existsByUnitId(id) || shipmentItemRepository.existsByUnitId(id)) {
            throw new BusinessException("Unit is used. Archive it instead.");
        }
        unitRepository.deleteById(id);
    }

    private UnitDto toDto(Unit u) {
        UnitDto dto = new UnitDto();
        dto.setId(u.getId());
        dto.setName(u.getName());
        dto.setState(u.getState());
        return dto;
    }
}