package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Supplier data transfer object")
public class SupplierDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Supplier unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(min = 1, max = 100, message = "Supplier name must be between 1 and 100 characters")
    @Schema(description = "Supplier name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Fresh Foods Inc.")
    private String name;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    @Schema(description = "Supplier address", example = "123 Market Street, City, State 12345")
    private String address;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Schema(description = "Supplier email address", example = "contact@freshfoods.com")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    @Schema(description = "Supplier phone number", example = "+1234567890")
    private String phone;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    @Schema(description = "Additional notes", example = "Delivers Monday-Friday, 8AM-5PM")
    private String notes;

    @Schema(description = "Whether supplier is active", example = "true")
    private Boolean isActive;
}
