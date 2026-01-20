package ru.ifmo.se.restaurant.billing.infrastructure.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.billing.application.dto.BillDto;
import ru.ifmo.se.restaurant.billing.application.dto.ErrorResponse;
import ru.ifmo.se.restaurant.billing.application.port.in.DeleteBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.GenerateBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.GetBillUseCase;
import ru.ifmo.se.restaurant.billing.application.port.in.UpdateBillUseCase;
import ru.ifmo.se.restaurant.billing.domain.valueobject.BillStatus;
import ru.ifmo.se.restaurant.billing.domain.valueobject.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Billing", description = "API для управления счетами ресторана")
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillingController {
    private final GetBillUseCase getBillUseCase;
    private final GenerateBillUseCase generateBillUseCase;
    private final UpdateBillUseCase updateBillUseCase;
    private final DeleteBillUseCase deleteBillUseCase;

    @Operation(summary = "Получить все счета", description = "Возвращает список всех счетов без пагинации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список счетов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<BillDto>> getAllBills() {
        return ResponseEntity.ok(getBillUseCase.getAllBills());
    }

    @Operation(summary = "Получить все счета с пагинацией и total count",
            description = "Возвращает страницу счетов с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница счетов успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/paged")
    public ResponseEntity<Page<BillDto>> getAllBillsPaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<BillDto> bills = getBillUseCase.getAllBillsPaginated(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(bills.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(bills.getTotalPages()));
        headers.add("X-Page-Number", String.valueOf(bills.getNumber()));
        headers.add("X-Page-Size", String.valueOf(bills.getSize()));

        return ResponseEntity.ok().headers(headers).body(bills);
    }

    @Operation(summary = "Получить счета для бесконечной прокрутки (без total count)",
            description = "Возвращает срез счетов для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез счетов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/infinite-scroll")
    public ResponseEntity<Slice<BillDto>> getAllBillsInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Slice<BillDto> bills = getBillUseCase.getAllBillsSlice(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(bills.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(bills.hasPrevious()));
        headers.add("X-Page-Number", String.valueOf(bills.getNumber()));
        headers.add("X-Page-Size", String.valueOf(bills.getSize()));

        return ResponseEntity.ok().headers(headers).body(bills);
    }

    @Operation(summary = "Получить счет по ID", description = "Возвращает счет по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Счет найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class))),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BillDto> getBillById(
            @Parameter(description = "ID счета", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(getBillUseCase.getBillById(id));
    }

    @Operation(summary = "Получить счет по ID заказа", description = "Возвращает счет связанный с указанным заказом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Счет найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class))),
            @ApiResponse(responseCode = "404", description = "Счет не найден для данного заказа",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/order/{orderId}")
    public ResponseEntity<BillDto> getBillByOrderId(
            @Parameter(description = "ID заказа", required = true, example = "1")
            @PathVariable Long orderId) {
        return ResponseEntity.ok(getBillUseCase.getBillByOrderId(orderId));
    }

    @Operation(summary = "Получить счета по статусу", description = "Возвращает список счетов с указанным статусом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список счетов успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BillDto>> getBillsByStatus(
            @Parameter(description = "Статус счета", required = true, example = "PENDING")
            @PathVariable BillStatus status) {
        return ResponseEntity.ok(getBillUseCase.getBillsByStatus(status));
    }

    @Operation(summary = "Сгенерировать счет для заказа",
            description = "Создает новый счет для указанного заказа с автоматическим расчетом налогов и сервисного сбора")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Счет успешно создан",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class))),
            @ApiResponse(responseCode = "409", description = "Счет для данного заказа уже существует",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Сервис заказов недоступен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/generate/{orderId}")
    public ResponseEntity<BillDto> generateBill(
            @Parameter(description = "ID заказа", required = true, example = "1")
            @PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(generateBillUseCase.generateBill(orderId));
    }

    @Operation(summary = "Применить скидку к счету", description = "Применяет скидку к счету в статусе PENDING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Скидка успешно применена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class))),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Нельзя применить скидку к счету не в статусе PENDING",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/discount")
    public ResponseEntity<BillDto> applyDiscount(
            @Parameter(description = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Сумма скидки", required = true, example = "100.00")
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(updateBillUseCase.applyDiscount(id, amount));
    }

    @Operation(summary = "Оплатить счет", description = "Отмечает счет как оплаченный указанным способом оплаты")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Счет успешно оплачен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class))),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Счет должен быть в статусе PENDING",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/pay")
    public ResponseEntity<BillDto> payBill(
            @Parameter(description = "ID счета", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Способ оплаты", required = true, example = "CASH")
            @RequestParam PaymentMethod paymentMethod) {
        return ResponseEntity.ok(updateBillUseCase.payBill(id, paymentMethod));
    }

    @Operation(summary = "Отменить счет", description = "Отменяет счет, если он еще не оплачен")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Счет успешно отменен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BillDto.class))),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Нельзя отменить оплаченный счет",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BillDto> cancelBill(
            @Parameter(description = "ID счета", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(updateBillUseCase.cancelBill(id));
    }

    @Operation(summary = "Удалить счет", description = "Полностью удаляет счет из системы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Счет успешно удален"),
            @ApiResponse(responseCode = "404", description = "Счет не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBill(
            @Parameter(description = "ID счета", required = true, example = "1")
            @PathVariable Long id) {
        deleteBillUseCase.deleteBill(id);
        return ResponseEntity.noContent().build();
    }
}
