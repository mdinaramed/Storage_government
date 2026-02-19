package com.example.warehouse.service;

import com.example.warehouse.dto.BalanceDto;
import com.example.warehouse.entity.Balance;
import com.example.warehouse.entity.Resource;
import com.example.warehouse.entity.Unit;
import com.example.warehouse.exception.BusinessException;
import com.example.warehouse.exception.NotFoundException;
import com.example.warehouse.repository.BalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceService {

    private final BalanceRepository balanceRepository;

    public BalanceService(BalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Transactional(readOnly = true)
    public List<Balance> search(List<Long> resourceIds, List<Long> unitIds) {
        List<Balance> all = balanceRepository.findAll();

        if (resourceIds != null && !resourceIds.isEmpty()) {
            all = all.stream()
                    .filter(b -> b.getResource() != null && b.getResource().getId() != null
                            && resourceIds.contains(b.getResource().getId()))
                    .toList();
        }

        if (unitIds != null && !unitIds.isEmpty()) {
            all = all.stream()
                    .filter(b -> b.getUnit() != null && b.getUnit().getId() != null
                            && unitIds.contains(b.getUnit().getId()))
                    .toList();
        }

        return all;
    }

    @Transactional(readOnly = true)
    public List<BalanceDto> searchDto(List<Long> resourceIds, List<Long> unitIds) {
        return search(resourceIds, unitIds).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void add(Resource resource, Unit unit, BigDecimal qty) {
        validateQty(qty);
        validateRefs(resource, unit);

        Balance bal = balanceRepository
                .findByResourceIdAndUnitId(resource.getId(), unit.getId())
                .orElseGet(() -> {
                    Balance b = new Balance();
                    b.setResource(resource);
                    b.setUnit(unit);
                    b.setAmount(BigDecimal.ZERO);
                    return b;
                });

        bal.setAmount(bal.getAmount().add(qty));
        balanceRepository.save(bal);
    }

    @Transactional
    public void subtract(Resource resource, Unit unit, BigDecimal qty) {
        validateQty(qty);
        validateRefs(resource, unit);

        Balance bal = balanceRepository
                .findByResourceIdAndUnitId(resource.getId(), unit.getId())
                .orElseThrow(() -> new NotFoundException("Balance not found for resource/unit"));

        BigDecimal next = bal.getAmount().subtract(qty);
        if (next.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Not enough balance for resource: " + safeName(resource));
        }

        bal.setAmount(next);
        balanceRepository.save(bal);
    }

    private void validateQty(BigDecimal qty) {
        if (qty == null || qty.signum() <= 0) {
            throw new BusinessException("Quantity must be > 0");
        }
    }

    private void validateRefs(Resource resource, Unit unit) {
        if (resource == null || resource.getId() == null) {
            throw new BusinessException("Resource is required");
        }
        if (unit == null || unit.getId() == null) {
            throw new BusinessException("Unit is required");
        }
    }

    private String safeName(Resource r) {
        return (r == null || r.getName() == null) ? "unknown" : r.getName();
    }

    private BalanceDto toDto(Balance b) {
        BalanceDto d = new BalanceDto();
        d.setId(b.getId());
        d.setAmount(b.getAmount());

        if (b.getResource() != null) {
            d.setResourceId(b.getResource().getId());
            d.setResourceName(b.getResource().getName());
        }
        if (b.getUnit() != null) {
            d.setUnitId(b.getUnit().getId());
            d.setUnitName(b.getUnit().getName());
        }
        return d;
    }
}