package ru.ifmo.se.restaurant.billing.application.port.in;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import ru.ifmo.se.restaurant.billing.application.dto.BillDto;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;

import java.util.List;

public interface GetBillUseCase {
    List<BillDto> getAllBills();
    BillDto getBillById(Long id);
    BillDto getBillByOrderId(Long orderId);
    List<BillDto> getBillsByStatus(BillStatus status);
    Page<BillDto> getAllBillsPaginated(int page, int size);
    Slice<BillDto> getAllBillsSlice(int page, int size);
    Page<BillDto> getBillsByStatusPaginated(BillStatus status, int page, int size);
}
