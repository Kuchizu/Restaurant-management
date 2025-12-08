package ru.ifmo.se.restaurant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.se.restaurant.model.TableStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Table data transfer object")
public class TableDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Table unique identifier", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long id;

    @NotNull(message = "Table number cannot be null")
    @Schema(description = "Table number", requiredMode = Schema.RequiredMode.REQUIRED, example = "15")
    private Integer tableNumber;

    @NotNull(message = "Capacity cannot be null")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Schema(description = "Maximum seating capacity", requiredMode = Schema.RequiredMode.REQUIRED, example = "4")
    private Integer capacity;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    @Schema(description = "Table location in restaurant", example = "Main dining area, near window")
    private String location;

    @Schema(description = "Current table status", example = "FREE")
    private TableStatus status;

    @Schema(description = "Whether table is active", example = "true")
    private Boolean isActive;
}
