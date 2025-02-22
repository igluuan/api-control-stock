package tech.devluan.api_control_stock.model.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductCreationDTO(
        @NotNull
        String name,
        @NotNull
        Integer quantity,
        @NotNull
        BigDecimal price
) {
}
