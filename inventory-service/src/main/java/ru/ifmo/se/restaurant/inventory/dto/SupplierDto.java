package ru.ifmo.se.restaurant.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Поставщик продуктов")
public class SupplierDto {
    @Schema(description = "ID поставщика (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "3")
    private Long id;

    @Schema(description = "Название компании поставщика", required = true, example = "ООО 'Мясная лавка'")
    private String name;

    @Schema(description = "Контактное лицо", example = "Иванов Иван Иванович")
    private String contactPerson;

    @Schema(description = "Телефон", example = "+7 (495) 123-45-67")
    private String phone;

    @Schema(description = "Email", example = "info@meatshop.ru")
    private String email;

    @Schema(description = "Адрес", example = "г. Москва, ул. Складская, д. 15")
    private String address;
}
