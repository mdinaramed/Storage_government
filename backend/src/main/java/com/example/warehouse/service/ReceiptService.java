package com.example.warehouse.service;

import com.example.warehouse.dto.ReceiptDto;
import com.example.warehouse.dto.ReceiptItemDto;
import com.example.warehouse.dto.ReceiptListItemDto;
import com.example.warehouse.entity.*;
import com.example.warehouse.exception.BusinessException;
import com.example.warehouse.exception.NotFoundException;
import com.example.warehouse.repository.ReceiptRepository;
import com.example.warehouse.repository.ResourceRepository;
import com.example.warehouse.repository.UnitRepository;
import com.example.warehouse.util.Normalize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ResourceRepository resourceRepository;
    private final UnitRepository unitRepository;
    private final BalanceService balanceService;

    public ReceiptService(ReceiptRepository receiptRepository,
                          ResourceRepository resourceRepository,
                          UnitRepository unitRepository,
                          BalanceService balanceService) {
        this.receiptRepository = receiptRepository;
        this.resourceRepository = resourceRepository;
        this.unitRepository = unitRepository;
        this.balanceService = balanceService;
    }


    @Transactional(readOnly = true)
    public ReceiptDto newDraft() {
        ReceiptDto dto = new ReceiptDto();
        dto.setDate(LocalDate.now());
        dto.setItems(new ArrayList<>());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<String> getAllNumbers() {
        return receiptRepository.findAll().stream()
                .map(Receipt::getNumber)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .sorted()
                .toList();
    }


    @Transactional(readOnly = true)
    public ReceiptDto getById(Long id) {
        Receipt r = receiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Receipt not found"));
        return toDto(r);
    }

    @Transactional(readOnly = true)
    public List<ReceiptListItemDto> searchDto(LocalDate from, LocalDate to,
                                              List<String> numbers,
                                              List<Long> resourceIds,
                                              List<Long> unitIds) {
        return search(from, to, numbers, resourceIds, unitIds).stream()
                .map(this::toListItemDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Receipt> search(LocalDate from, LocalDate to,
                                List<String> numbers,
                                List<Long> resourceIds,
                                List<Long> unitIds) {

        List<Receipt> base = receiptRepository.findAll();

        if (from != null) base = base.stream().filter(r -> !r.getDate().isBefore(from)).toList();
        if (to != null) base = base.stream().filter(r -> !r.getDate().isAfter(to)).toList();

        if (numbers != null && !numbers.isEmpty()) {
            Set<String> set = numbers.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .collect(Collectors.toSet());

            base = base.stream()
                    .filter(r -> r.getNumber() != null && set.contains(r.getNumber().toLowerCase()))
                    .toList();
        }

        if (resourceIds != null && !resourceIds.isEmpty()) {
            Set<Long> resSet = new HashSet<>(resourceIds);
            base = base.stream()
                    .filter(r -> r.getItems() != null && r.getItems().stream()
                            .anyMatch(i -> i.getResource() != null && resSet.contains(i.getResource().getId())))
                    .toList();
        }

        if (unitIds != null && !unitIds.isEmpty()) {
            Set<Long> unitSet = new HashSet<>(unitIds);
            base = base.stream()
                    .filter(r -> r.getItems() != null && r.getItems().stream()
                            .anyMatch(i -> i.getUnit() != null && unitSet.contains(i.getUnit().getId())))
                    .toList();
        }

        return base;
    }


    @Transactional
    public ReceiptDto create(ReceiptDto dto) {
        validateReceiptDto(dto);

        String number = Normalize.normalize(dto.getNumber());
        if (receiptRepository.existsByNumberIgnoreCase(number)) {
            throw new BusinessException("Receipt with this number already exists");
        }

        LocalDate date = dto.getDate() == null ? LocalDate.now() : dto.getDate();

        Receipt receipt = Receipt.builder()
                .number(number)
                .date(date)
                .build();

        List<ReceiptItem> items = mapReceiptItems(dto.getItems(), receipt);
        receipt.setItems(items);

        Receipt saved = receiptRepository.save(receipt);

        for (ReceiptItem it : items) {
            balanceService.add(it.getResource(), it.getUnit(), it.getQuantity());
        }

        return toDto(saved);
    }

    @Transactional
    public ReceiptDto updateAndReturn(Long id, ReceiptDto dto) {
        update(id, dto);
        return getById(id);
    }

    @Transactional
    public void update(Long id, ReceiptDto dto) {
        validateReceiptDto(dto);

        Receipt existing = receiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Receipt not found"));

        String number = Normalize.normalize(dto.getNumber());
        if (!existing.getNumber().equalsIgnoreCase(number)
                && receiptRepository.existsByNumberIgnoreCase(number)) {
            throw new BusinessException("Receipt with this number already exists");
        }

        LocalDate date = dto.getDate() == null ? existing.getDate() : dto.getDate();

        if (existing.getItems() != null) {
            for (ReceiptItem old : existing.getItems()) {
                balanceService.subtract(old.getResource(), old.getUnit(), old.getQuantity());
            }
        }

        List<ReceiptItem> newItems = mapReceiptItems(dto.getItems(), existing);

        existing.setNumber(number);
        existing.setDate(date);
        existing.setItems(newItems);

        receiptRepository.save(existing);

        for (ReceiptItem it : newItems) {
            balanceService.add(it.getResource(), it.getUnit(), it.getQuantity());
        }
    }

    @Transactional
    public void delete(Long id) {
        Receipt existing = receiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Receipt not found"));

        if (existing.getItems() != null) {
            for (ReceiptItem old : existing.getItems()) {
                balanceService.subtract(old.getResource(), old.getUnit(), old.getQuantity());
            }
        }

        receiptRepository.delete(existing);
    }

    private void validateReceiptDto(ReceiptDto dto) {
        if (dto == null) throw new BusinessException("Receipt is required");

        String number = Normalize.normalize(dto.getNumber());
        if (number == null || number.isBlank()) throw new BusinessException("Number is required");

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("Receipt cannot be empty");
        }
    }

    private List<ReceiptItem> mapReceiptItems(List<ReceiptItemDto> itemDtos, Receipt receipt) {
        if (itemDtos == null) return new ArrayList<>();

        List<ReceiptItem> items = new ArrayList<>();
        for (ReceiptItemDto d : itemDtos) {
            if (d == null) continue;

            if (d.getResourceId() == null || d.getUnitId() == null || d.getQuantity() == null) {
                throw new BusinessException("Receipt item fields are required");
            }
            if (d.getQuantity().signum() <= 0) throw new BusinessException("Quantity must be > 0");

            Resource res = resourceRepository.findById(d.getResourceId())
                    .orElseThrow(() -> new BusinessException("Resource not found"));

            Unit unit = unitRepository.findById(d.getUnitId())
                    .orElseThrow(() -> new BusinessException("Unit not found"));

            if (res.getState() == EntityState.ARCHIVED) throw new BusinessException("Resource is archived");
            if (unit.getState() == EntityState.ARCHIVED) throw new BusinessException("Unit is archived");

            items.add(ReceiptItem.builder()
                    .receipt(receipt)
                    .resource(res)
                    .unit(unit)
                    .quantity(d.getQuantity())
                    .build());
        }

        if (items.isEmpty()) throw new BusinessException("Receipt cannot be empty");
        return items;
    }

    private ReceiptDto toDto(Receipt r) {
        ReceiptDto dto = new ReceiptDto();
        dto.setId(r.getId());
        dto.setNumber(r.getNumber());
        dto.setDate(r.getDate());

        List<ReceiptItemDto> items = (r.getItems() == null ? List.<ReceiptItem>of() : r.getItems())
                .stream()
                .map(it -> {
                    ReceiptItemDto d = new ReceiptItemDto();
                    d.setResourceId(it.getResource().getId());
                    d.setUnitId(it.getUnit().getId());
                    d.setQuantity(it.getQuantity());
                    return d;
                })
                .toList();

        dto.setItems(new ArrayList<>(items));
        return dto;
    }

    private ReceiptListItemDto toListItemDto(Receipt r) {
        ReceiptListItemDto dto = new ReceiptListItemDto();
        dto.setId(r.getId());
        dto.setNumber(r.getNumber());
        dto.setDate(r.getDate());
        return dto;
    }
}