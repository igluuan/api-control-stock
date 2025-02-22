package tech.devluan.api_control_stock.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import tech.devluan.api_control_stock.model.Product;
import tech.devluan.api_control_stock.model.dto.ProductCreationDTO;
import tech.devluan.api_control_stock.model.dto.ProductResponseDTO;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "productQuantity", source = "quantity")
    @Mapping(target = "productPrice", source = "price")
    @Mapping(target = "productName", source = "name")
    Product toEntity(ProductCreationDTO productCreationDTO);

    @Mapping(source ="productName", target = "name")
    @Mapping(source ="productQuantity", target = "quantity")
    @Mapping(source ="productPrice", target = "price")
    ProductCreationDTO toCreationDTO(Product product);

    @Mapping(source ="productName", target = "name")
    @Mapping(source ="productQuantity", target = "quantity")
    @Mapping(source ="productPrice", target = "price")
    @Mapping(source ="updatedAt", target = "lastUpdate")
    ProductResponseDTO toResponseDTO(Product product);

    default Page<ProductResponseDTO> toPageDTO(Page<Product> productPage) {
        return productPage.map(this::toResponseDTO);
    }
}

