package ru.ifmo.se.restaurant.kitchen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.se.restaurant.kitchen.dto.ErrorResponse;
import ru.ifmo.se.restaurant.kitchen.dto.KitchenQueueDto;
import ru.ifmo.se.restaurant.kitchen.entity.DishStatus;
import ru.ifmo.se.restaurant.kitchen.service.KitchenService;

import java.util.List;

@Tag(name = "Kitchen Service", description = "API для управления кухонной очередью")
@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
public class KitchenController {
    private final KitchenService kitchenService;

    @Operation(
        summary = "Добавить блюдо в очередь",
        description = "Добавляет новое блюдо в кухонную очередь со статусом PENDING",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Только обязательные поля",
            content = @Content(
                schema = @Schema(implementation = KitchenQueueDto.class),
                examples = @ExampleObject(
                    name = "Минимальный запрос",
                    value = """
                        {
                          "orderId": 10,
                          "orderItemId": 25,
                          "dishName": "Стейк",
                          "quantity": 1
                        }
                        """
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Блюдо успешно добавлено в очередь",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KitchenQueueDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/queue")
    @ResponseStatus(HttpStatus.CREATED)
    public KitchenQueueDto addToQueue(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные блюда для добавления в очередь",
                    required = true
            )
            @Valid @RequestBody KitchenQueueDto dto) {
        return kitchenService.addToQueue(dto);
    }

    @Operation(summary = "Получить активную очередь",
            description = "Возвращает список блюд в статусах PENDING и IN_PROGRESS, отсортированных по времени создания")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Активная очередь успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KitchenQueueDto.class)))
    })
    @GetMapping("/queue")
    public List<KitchenQueueDto> getActiveQueue() {
        return kitchenService.getActiveQueue();
    }

    @Operation(summary = "Получить всю очередь без пагинации",
            description = "Возвращает все элементы кухонной очереди независимо от статуса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Очередь успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KitchenQueueDto.class)))
    })
    @GetMapping("/queue/all")
    public List<KitchenQueueDto> getAllQueue() {
        return kitchenService.getAllQueue();
    }

    @Operation(summary = "Получить всю очередь с пагинацией и total count",
            description = "Возвращает страницу элементов очереди с информацией об общем количестве в заголовке X-Total-Count. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница очереди успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/queue/paged")
    public ResponseEntity<Page<KitchenQueueDto>> getAllQueuePaged(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Page<KitchenQueueDto> queuePage = kitchenService.getAllQueueItemsPaginated(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(queuePage.getTotalElements()));
        headers.add("X-Total-Pages", String.valueOf(queuePage.getTotalPages()));
        headers.add("X-Page-Number", String.valueOf(queuePage.getNumber()));
        headers.add("X-Page-Size", String.valueOf(queuePage.getSize()));

        return ResponseEntity.ok().headers(headers).body(queuePage);
    }

    @Operation(summary = "Получить очередь для бесконечной прокрутки (без total count)",
            description = "Возвращает срез элементов очереди для реализации бесконечной прокрутки без подсчета общего количества. Максимум 50 записей за запрос.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Срез очереди успешно получен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Slice.class)))
    })
    @GetMapping("/queue/infinite-scroll")
    public ResponseEntity<Slice<KitchenQueueDto>> getAllQueueInfiniteScroll(
            @Parameter(description = "Номер страницы (начиная с 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 50)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        Slice<KitchenQueueDto> queueSlice = kitchenService.getAllQueueItemsSlice(page, size);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Has-Next", String.valueOf(queueSlice.hasNext()));
        headers.add("X-Has-Previous", String.valueOf(queueSlice.hasPrevious()));
        headers.add("X-Page-Number", String.valueOf(queueSlice.getNumber()));
        headers.add("X-Page-Size", String.valueOf(queueSlice.getSize()));

        return ResponseEntity.ok().headers(headers).body(queueSlice);
    }

    @Operation(summary = "Получить элемент очереди по ID",
            description = "Возвращает элемент кухонной очереди по указанному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Элемент очереди найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KitchenQueueDto.class))),
            @ApiResponse(responseCode = "404", description = "Элемент очереди не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/queue/{id}")
    public KitchenQueueDto getQueueItemById(
            @Parameter(description = "ID элемента очереди", required = true, example = "1")
            @PathVariable Long id) {
        return kitchenService.getQueueItemById(id);
    }

    @Operation(summary = "Обновить статус блюда",
            description = "Обновляет статус приготовления блюда (PENDING -> IN_PROGRESS -> READY -> SERVED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KitchenQueueDto.class))),
            @ApiResponse(responseCode = "404", description = "Элемент очереди не найден",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/queue/{id}/status")
    public KitchenQueueDto updateStatus(
            @Parameter(description = "ID элемента очереди", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Новый статус блюда", required = true, example = "IN_PROGRESS")
            @RequestParam DishStatus status) {
        return kitchenService.updateStatus(id, status);
    }

    @Operation(summary = "Получить очередь по ID заказа",
            description = "Возвращает все элементы очереди для указанного заказа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Очередь для заказа успешно получена",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = KitchenQueueDto.class)))
    })
    @GetMapping("/queue/order/{orderId}")
    public List<KitchenQueueDto> getQueueByOrderId(
            @Parameter(description = "ID заказа", required = true, example = "1")
            @PathVariable Long orderId) {
        return kitchenService.getQueueByOrderId(orderId);
    }
}
