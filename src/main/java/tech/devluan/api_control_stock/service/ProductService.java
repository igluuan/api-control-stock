package tech.devluan.api_control_stock.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.devluan.api_control_stock.exceptions.ProductErrorExceptions;
import tech.devluan.api_control_stock.model.Product;
import tech.devluan.api_control_stock.model.dto.ProductCreationDTO;
import tech.devluan.api_control_stock.model.dto.ProductResponseDTO;
import tech.devluan.api_control_stock.repository.ProductRepository;
import tech.devluan.api_control_stock.service.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponseDTO createNewProduct(ProductCreationDTO productCreationDTO){
        if(productCreationDTO.name() == null
                && productCreationDTO.price() == null
                && productCreationDTO.quantity() == null){
            logger.warn("Todos os campos são necessários para a criação de um novo produto!");
            throw new ProductErrorExceptions("Preencha todos os campos");
        }
        logger.info("Criando um novo produto {}:", productCreationDTO.name());
        try {
            Product newProduct = productMapper.toEntity(productCreationDTO);
            productRepository.save(newProduct);
            logger.info("Produto criado com sucesso! identificador do produto: {}", newProduct.getProductId());
            return productMapper.toResponseDTO(newProduct);
        } catch (Exception e) {
            logger.error("error ao criar o produto: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
