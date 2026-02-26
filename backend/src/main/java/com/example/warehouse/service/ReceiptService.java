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

import java.math.BigDecimal;
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
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
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
    public List<ReceiptListItemDto> searchListDto(LocalDate from, LocalDate to,
                                                  List<String> numbers,
                                                  List<Long> resourceIds,
                                                  List<Long> unitIds) {

        List<Receipt> base = receiptRepository.findAllWithItems();

        base = applyFilters(base, from, to, numbers, resourceIds, unitIds);

        return base.stream()
                .map(this::toListItemDtoWithItems)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Receipt> search(LocalDate from, LocalDate to,
                                List<String> numbers,
                                List<Long> resourceIds,
                                List<Long> unitIds) {

        List<Receipt> base = receiptRepository.findAllWithItems();
        return applyFilters(base, from, to, numbers, resourceIds, unitIds);
    }

    private List<Receipt> applyFilters(List<Receipt> base,
                                       LocalDate from, LocalDate to,
                                       List<String> numbers,
                                       List<Long> resourceIds,
                                       List<Long> unitIds) {

        if (from != null) {
            base = base.stream()
                    .filter(r -> r.getDate() != null && !r.getDate().isBefore(from))
                    .toList();
        }
        if (to != null) {
            base = base.stream()
                    .filter(r -> r.getDate() != null && !r.getDate().isAfter(to))
                    .toList();
        }

        if (numbers != null && !numbers.isEmpty()) {
            Set<String> set = numbers.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.trim().toLowerCase())
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());

            base = base.stream()
                    .filter(r -> r.getNumber() != null && set.contains(r.getNumber().toLowerCase()))
                    .toList();
        }

        if ((resourceIds != null && !resourceIds.isEmpty()) || (unitIds != null && !unitIds.isEmpty())) {
            Set<Long> resSet = (resourceIds == null) ? Set.of() : new HashSet<>(resourceIds);
            Set<Long> unitSet = (unitIds == null) ? Set.of() : new HashSet<>(unitIds);

            base = base.stream()
                    .filter(r -> {
                        List<ReceiptItem> its = r.getItems();
                        if (its == null || its.isEmpty()) return false;
                        return its.stream().anyMatch(i -> {
                            boolean rOk = resSet.isEmpty() || (i.getResource() != null && resSet.contains(i.getResource().getId()));
                            boolean uOk = unitSet.isEmpty() || (i.getUnit() != null && unitSet.contains(i.getUnit().getId()));
                            return rOk && uOk;
                        });
                    })
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

        LocalDate date = (dto.getDate() == null) ? LocalDate.now() : dto.getDate();

        Receipt receipt = Receipt.builder()
                .number(number)
                .date(date)
                .build();

        List<ReceiptItem> items = mapReceiptItems(dto.getItems(), receipt);
        receipt.setItems(items);

        Receipt saved = receiptRepository.save(receipt);

        if (items != null) {
            for (ReceiptItem it : items) {
                balanceService.add(it.getResource(), it.getUnit(), it.getQuantity());
            }
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
        if (existing.getNumber() != null
                && !existing.getNumber().equalsIgnoreCase(number)
                && receiptRepository.existsByNumberIgnoreCase(number)) {
            throw new BusinessException("Receipt with this number already exists");
        }

        LocalDate date = (dto.getDate() == null) ? existing.getDate() : dto.getDate();

        Map<RuKey, BigDecimal> oldTotals = aggregateReceiptItems(existing.getItems());
        List<ReceiptItem> newItems = mapReceiptItems(dto.getItems(), existing);
        Map<RuKey, BigDecimal> newTotals = aggregateReceiptItems(newItems);

        Map<RuKey, BigDecimal> delta = new HashMap<>();
        Set<RuKey> allKeys = new HashSet<>();
        allKeys.addAll(oldTotals.keySet());
        allKeys.addAll(newTotals.keySet());

        for (RuKey k : allKeys) {
            BigDecimal oldQ = oldTotals.getOrDefault(k, BigDecimal.ZERO);
            BigDecimal newQ = newTotals.getOrDefault(k, BigDecimal.ZERO);
            BigDecimal d = newQ.subtract(oldQ);
            if (d.signum() != 0) delta.put(k, d);
        }

        for (Map.Entry<RuKey, BigDecimal> e : delta.entrySet()) {
            BigDecimal d = e.getValue();
            if (d.signum() < 0) {
                RuKey k = e.getKey();
                BigDecimal needSubtract = d.abs();
                BigDecimal current = getBalanceAmountOrZero(k.resourceId, k.unitId);
                if (current.subtract(needSubtract).signum() < 0) {
                    throw new BusinessException(
                            "Not enough stock to update receipt. Resource/unit would go negative: resourceId="
                                    + k.resourceId + ", unitId=" + k.unitId
                    );
                }
            }
        }

        for (Map.Entry<RuKey, BigDecimal> e : delta.entrySet()) {
            BigDecimal d = e.getValue();
            RuKey k = e.getKey();

            Resource res = resourceRepository.findById(k.resourceId)
                    .orElseThrow(() -> new BusinessException("Resource not found"));
            Unit unit = unitRepository.findById(k.unitId)
                    .orElseThrow(() -> new BusinessException("Unit not found"));

            if (d.signum() < 0) balanceService.subtract(res, unit, d.abs());
            if (d.signum() > 0) balanceService.add(res, unit, d);
        }

        existing.setNumber(number);
        existing.setDate(date);
        existing.setItems(newItems);

        receiptRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Receipt existing = receiptRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Receipt not found"));

        Map<RuKey, BigDecimal> totals = aggregateReceiptItems(existing.getItems());

        for (Map.Entry<RuKey, BigDecimal> e : totals.entrySet()) {
            RuKey k = e.getKey();
            BigDecimal needSubtract = e.getValue();
            BigDecimal current = getBalanceAmountOrZero(k.resourceId, k.unitId);
            if (current.subtract(needSubtract).signum() < 0) {
                throw new BusinessException(
                        "Not enough stock to delete receipt. Resource/unit would go negative: resourceId="
                                + k.resourceId + ", unitId=" + k.unitId
                );
            }
        }

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

    }

    private List<ReceiptItem> mapReceiptItems(List<ReceiptItemDto> itemDtos, Receipt receipt) {
        if (itemDtos == null) return new ArrayList<>();

        List<ReceiptItem> items = new ArrayList<>();
        for (ReceiptItemDto d : itemDtos) {
            if (d == null) continue;

            if (d.getResourceId() == null || d.getUnitId() == null || d.getQuantity() == null) continue;

            if (d.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Quantity must be > 0");
            }

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

        return items;
    }

    private Map<RuKey, BigDecimal> aggregateReceiptItems(List<ReceiptItem> items) {
        Map<RuKey, BigDecimal> m = new HashMap<>();
        if (items == null) return m;

        for (ReceiptItem it : items) {
            if (it == null || it.getResource() == null || it.getUnit() == null || it.getQuantity() == null) continue;
            Long rid = it.getResource().getId();
            Long uid = it.getUnit().getId();
            if (rid == null || uid == null) continue;

            RuKey k = new RuKey(rid, uid);
            m.merge(k, it.getQuantity(), BigDecimal::add);
        }

        m.entrySet().removeIf(e -> e.getValue() == null || e.getValue().signum() == 0);
        return m;
    }

    private BigDecimal getBalanceAmountOrZero(Long resourceId, Long unitId) {
        if (resourceId == null || unitId == null) return BigDecimal.ZERO;

        List<Balance> list = balanceService.search(
                Collections.singletonList(resourceId),
                Collections.singletonList(unitId)
        );
        if (list == null || list.isEmpty()) return BigDecimal.ZERO;

        Balance b = list.get(0);
        return (b.getAmount() == null) ? BigDecimal.ZERO : b.getAmount();
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
                    d.setResourceId(it.getResource() == null ? null : it.getResource().getId());
                    d.setUnitId(it.getUnit() == null ? null : it.getUnit().getId());
                    d.setQuantity(it.getQuantity());
                    return d;
                })
                .toList();

        dto.setItems(new ArrayList<>(items));
        return dto;
    }

    private ReceiptListItemDto toListItemDtoWithItems(Receipt r) {
        ReceiptListItemDto dto = new ReceiptListItemDto();
        dto.setId(r.getId());
        dto.setNumber(r.getNumber());
        dto.setDate(r.getDate());

        List<ReceiptListItemDto.Item> list = new ArrayList<>();
        if (r.getItems() != null) {
            for (ReceiptItem it : r.getItems()) {
                if (it == null) continue;

                ReceiptListItemDto.Item x = new ReceiptListItemDto.Item();
                if (it.getResource() != null) {
                    x.setResourceId(it.getResource().getId());
                    x.setResourceName(it.getResource().getName());
                }
                if (it.getUnit() != null) {
                    x.setUnitId(it.getUnit().getId());
                    x.setUnitName(it.getUnit().getName());
                }
                x.setQuantity(it.getQuantity());
                list.add(x);
            }
        }
        dto.setItems(list);
        return dto;
    }

    private static final class RuKey {
        private final Long resourceId;
        private final Long unitId;

        private RuKey(Long resourceId, Long unitId) {
            this.resourceId = resourceId;
            this.unitId = unitId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RuKey)) return false;
            RuKey ruKey = (RuKey) o;
            return Objects.equals(resourceId, ruKey.resourceId) && Objects.equals(unitId, ruKey.unitId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceId, unitId);
        }
    }
}