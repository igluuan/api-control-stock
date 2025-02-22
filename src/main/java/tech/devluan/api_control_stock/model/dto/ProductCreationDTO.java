package tech.devluan.api_control_stock.model.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductCreationDTO(
        String name,
        Integer quantity,
        BigDecimal price
) {
}
