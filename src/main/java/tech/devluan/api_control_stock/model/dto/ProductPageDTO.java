package tech.devluan.api_control_stock.model.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record ProductPageDTO(
        List<ProductResponseDTO> products,
        int currentPage,
        int totalPages,
        long totalElements
) {
    public static ProductPageDTO from(Page<ProductResponseDTO> page) {
        return new ProductPageDTO(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
