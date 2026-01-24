package ru.ifmo.se.restaurant.inventory.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.inventory.application.dto.SupplierDto;

import java.util.List;

public interface ManageSupplierUseCase {
    List<SupplierDto> getAllSuppliers();
    Page<SupplierDto> getAllSuppliersPaginated(int page, int size);
    Slice<SupplierDto> getAllSuppliersSlice(int page, int size);
    SupplierDto getSupplierById(Long id);
    SupplierDto createSupplier(SupplierDto dto);
    SupplierDto updateSupplier(Long id, SupplierDto dto);
    void deleteSupplier(Long id);
}
