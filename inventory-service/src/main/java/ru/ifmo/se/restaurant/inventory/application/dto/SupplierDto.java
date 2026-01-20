package ru.ifmo.se.restaurant.inventory.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.inventory.domain.entity.Supplier;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Поставщик продуктов")
public class SupplierDto {
    @Schema(description = "ID поставщика (заполняется автоматически)", accessMode = Schema.AccessMode.READ_ONLY, example = "3")
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Size(min = 2, max = 200, message = "Supplier name must be between 2 and 200 characters")
    @Schema(description = "Название компании поставщика", required = true, example = "ООО 'Мясная лавка'")
    private String name;

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    @Schema(description = "Контактное лицо", example = "Иванов Иван Иванович")
    private String contactPerson;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Schema(description = "Телефон", example = "+7 (495) 123-45-67")
    private String phone;

    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Schema(description = "Email", example = "info@meatshop.ru")
    private String email;

    @Size(max = 300, message = "Address cannot exceed 300 characters")
    @Schema(description = "Адрес", example = "г. Москва, ул. Складская, д. 15")
    private String address;

    public static SupplierDto fromDomain(Supplier supplier) {
        return SupplierDto.builder()
            .id(supplier.getId())
            .name(supplier.getName())
            .contactPerson(supplier.getContactPerson())
            .phone(supplier.getPhone())
            .email(supplier.getEmail())
            .address(supplier.getAddress())
            .build();
    }
}
