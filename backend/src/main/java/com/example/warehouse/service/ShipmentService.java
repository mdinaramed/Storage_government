package com.example.warehouse.service;

import com.example.warehouse.dto.ShipmentDto;
import com.example.warehouse.dto.ShipmentItemDto;
import com.example.warehouse.entity.*;
import com.example.warehouse.exception.BusinessException;
import com.example.warehouse.exception.NotFoundException;
import com.example.warehouse.repository.*;
import com.example.warehouse.util.Normalize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ClientRepository clientRepository;
    private final ResourceRepository resourceRepository;
    private final UnitRepository unitRepository;
    private final BalanceService balanceService;

    public ShipmentService(ShipmentRepository shipmentRepository,
                           ClientRepository clientRepository,
                           ResourceRepository resourceRepository,
                           UnitRepository unitRepository,
                           BalanceService balanceService) {
        this.shipmentRepository = shipmentRepository;
        this.clientRepository = clientRepository;
        this.resourceRepository = resourceRepository;
        this.unitRepository = unitRepository;
        this.balanceService = balanceService;
    }

    @Transactional(readOnly = true)
    public List<String> getAllNumbers() {
        return shipmentRepository.findAll().stream()
                .map(Shipment::getNumber)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    public ShipmentDto newDraft() {
        ShipmentDto dto = new ShipmentDto();
        dto.setDate(LocalDate.now());
        dto.setState(ShipmentState.DRAFT);
        dto.setItems(new ArrayList<>());
        return dto;
    }

    @Transactional(readOnly = true)
    public ShipmentDto getById(Long id) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));
        return toDto(s);
    }

    @Transactional(readOnly = true)
    public List<Shipment> search(LocalDate from,
                                 LocalDate to,
                                 List<String> numbers,
                                 List<Long> resourceIds,
                                 List<Long> unitIds,
                                 ShipmentState state) {

        List<Shipment> base = shipmentRepository.findAll();

        if (from != null) {
            base = base.stream()
                    .filter(s -> s.getDate() != null && !s.getDate().isBefore(from))
                    .toList();
        }

        if (to != null) {
            base = base.stream()
                    .filter(s -> s.getDate() != null && !s.getDate().isAfter(to))
                    .toList();
        }

        if (numbers != null && !numbers.isEmpty()) {
            Set<String> nset = numbers.stream()
                    .filter(Objects::nonNull)
                    .map(x -> x.trim().toLowerCase())
                    .collect(Collectors.toSet());

            base = base.stream()
                    .filter(s -> s.getNumber() != null && nset.contains(s.getNumber().toLowerCase()))
                    .toList();
        }

        if (state != null) {
            base = base.stream()
                    .filter(s -> s.getState() == state)
                    .toList();
        }

        if ((resourceIds != null && !resourceIds.isEmpty()) || (unitIds != null && !unitIds.isEmpty())) {
            Set<Long> rset = (resourceIds == null) ? Set.of() : new HashSet<>(resourceIds);
            Set<Long> uset = (unitIds == null) ? Set.of() : new HashSet<>(unitIds);

            base = base.stream().filter(s -> s.getItems() != null && s.getItems().stream().anyMatch(it -> {
                boolean rOk = rset.isEmpty() || (it.getResource() != null && rset.contains(it.getResource().getId()));
                boolean uOk = uset.isEmpty() || (it.getUnit() != null && uset.contains(it.getUnit().getId()));
                return rOk && uOk;
            })).toList();
        }

        return base;
    }

    @Transactional
    public ShipmentDto create(ShipmentDto dto) {
        Shipment s = buildFromDto(dto, null);
        s.setState(ShipmentState.DRAFT);
        shipmentRepository.save(s);
        return toDto(s);
    }

    @Transactional
    public void update(Long id, ShipmentDto dto) {
        Shipment existing = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        if (existing.getState() == ShipmentState.SIGNED) {
            throw new BusinessException("Signed shipment cannot be edited. Revoke first.");
        }

        Shipment updated = buildFromDto(dto, existing);

        existing.setNumber(updated.getNumber());
        existing.setDate(updated.getDate());
        existing.setClient(updated.getClient());
        existing.setItems(updated.getItems());

        shipmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        if (s.getState() == ShipmentState.SIGNED) {
            throw new BusinessException("Signed shipment cannot be deleted. Revoke first.");
        }
        shipmentRepository.delete(s);
    }

    @Transactional
    public void sign(Long id) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        if (s.getState() == ShipmentState.SIGNED) return;

        if (s.getItems() == null || s.getItems().isEmpty()) {
            throw new BusinessException("Shipment cannot be empty");
        }

        for (ShipmentItem it : s.getItems()) {
            balanceService.subtract(it.getResource(), it.getUnit(), it.getQuantity());
        }

        s.setState(ShipmentState.SIGNED);
        shipmentRepository.save(s);
    }

    @Transactional
    public void revoke(Long id) {
        Shipment s = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        if (s.getState() == ShipmentState.DRAFT) return;

        for (ShipmentItem it : s.getItems()) {
            balanceService.add(it.getResource(), it.getUnit(), it.getQuantity());
        }

        s.setState(ShipmentState.DRAFT);
        shipmentRepository.save(s);
    }

    private Shipment buildFromDto(ShipmentDto dto, Shipment existing) {
        if (dto == null) throw new BusinessException("Shipment is required");

        String number = Normalize.normalize(dto.getNumber());
        if (number == null || number.isBlank()) throw new BusinessException("Number is required");

        if (existing == null) {
            if (shipmentRepository.existsByNumberIgnoreCase(number)) {
                throw new BusinessException("Shipment number exists");
            }
        } else {
            if (!existing.getNumber().equalsIgnoreCase(number)
                    && shipmentRepository.existsByNumberIgnoreCase(number)) {
                throw new BusinessException("Shipment number exists");
            }
        }

        if (dto.getClientId() == null) throw new BusinessException("Client is required");
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new BusinessException("Client not found"));
        if (client.getState() == EntityState.ARCHIVED) throw new BusinessException("Client is archived");

        LocalDate date = (dto.getDate() == null) ? LocalDate.now() : dto.getDate();

        List<ShipmentItem> items = mapItems(dto.getItems());
        if (items.isEmpty()) throw new BusinessException("Shipment cannot be empty");

        Shipment s = new Shipment();
        s.setNumber(number);
        s.setClient(client);
        s.setDate(date);
        s.setState(existing == null ? ShipmentState.DRAFT : existing.getState());
        s.setItems(items);

        return s;
    }

    private List<ShipmentItem> mapItems(List<ShipmentItemDto> dtos) {
        if (dtos == null) return new ArrayList<>();

        List<ShipmentItem> items = new ArrayList<>();
        for (ShipmentItemDto d : dtos) {
            if (d == null) continue;

            if (d.getResourceId() == null || d.getUnitId() == null || d.getQuantity() == null) {
                throw new BusinessException("Shipment item fields are required");
            }
            if (d.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Quantity must be > 0");
            }

            Resource res = resourceRepository.findById(d.getResourceId())
                    .orElseThrow(() -> new BusinessException("Resource not found"));
            Unit unit = unitRepository.findById(d.getUnitId())
                    .orElseThrow(() -> new BusinessException("Unit not found"));

            if (res.getState() == EntityState.ARCHIVED) throw new BusinessException("Resource is archived");
            if (unit.getState() == EntityState.ARCHIVED) throw new BusinessException("Unit is archived");

            ShipmentItem it = new ShipmentItem();
            it.setResource(res);
            it.setUnit(unit);
            it.setQuantity(d.getQuantity());
            items.add(it);
        }
        return items;
    }

    private ShipmentDto toDto(Shipment s) {
        ShipmentDto dto = new ShipmentDto();
        dto.setId(s.getId());
        dto.setNumber(s.getNumber());
        dto.setDate(s.getDate());
        dto.setClientId(s.getClient() == null ? null : s.getClient().getId());
        dto.setState(s.getState());

        List<ShipmentItemDto> items = new ArrayList<>();
        if (s.getItems() != null) {
            for (ShipmentItem it : s.getItems()) {
                ShipmentItemDto d = new ShipmentItemDto();
                d.setResourceId(it.getResource() == null ? null : it.getResource().getId());
                d.setUnitId(it.getUnit() == null ? null : it.getUnit().getId());
                d.setQuantity(it.getQuantity());
                items.add(d);
            }
        }
        dto.setItems(items);
        return dto;
    }
}