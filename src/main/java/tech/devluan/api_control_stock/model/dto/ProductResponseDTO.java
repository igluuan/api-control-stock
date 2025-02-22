package tech.devluan.api_control_stock.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponseDTO(
        String name,
        Integer quantity,
        BigDecimal price,
        LocalDateTime lastUpdate
) {
}
