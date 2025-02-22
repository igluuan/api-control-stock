package tech.devluan.api_control_stock.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.devluan.api_control_stock.model.dto.ProductCreationDTO;
import tech.devluan.api_control_stock.model.dto.ProductResponseDTO;
import tech.devluan.api_control_stock.service.ProductService;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody ProductCreationDTO productCreationDTO){
        var ProductResponseDTO = productService.createNewProduct(productCreationDTO);
        return ResponseEntity.ok(ProductResponseDTO);
    }
}
