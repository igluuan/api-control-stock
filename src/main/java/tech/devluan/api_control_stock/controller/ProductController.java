package tech.devluan.api_control_stock.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.devluan.api_control_stock.exceptions.*;
import tech.devluan.api_control_stock.model.Product;
import tech.devluan.api_control_stock.model.dto.ProductCreationDTO;
import tech.devluan.api_control_stock.model.dto.ProductPageDTO;
import tech.devluan.api_control_stock.model.dto.ProductResponseDTO;
import tech.devluan.api_control_stock.service.ProductService;
import tech.devluan.api_control_stock.service.mapper.ProductMapper;

/**
 * Controlador REST responsável por gerenciar operações relacionadas a produtos.
 * Todas as requisições são mapeadas sob o caminho base "/api/products".
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;
    private final ProductMapper productMapper;

    /**
     * Cria um novo produto com base nos dados fornecidos.
     *
     * @param productCreationDTO Dados do produto a ser criado
     * @return Resposta HTTP 201 com o DTO do produto criado
     * @throws ProductInvalidDataException se os dados fornecidos forem inválidos
     * @throws ProductConflictException se houver conflito de dados (ex.: duplicidade)
     */
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody @Valid ProductCreationDTO productCreationDTO) {
        logger.info("Recebida requisição para criar novo produto: {}", productCreationDTO);
        ProductResponseDTO productResponseDTO = productService.createNewProduct(productCreationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDTO);
    }

    /**
     * Busca todos os produtos com suporte a paginação.
     *
     * @param size Tamanho da página (padrão: 10)
     * @param page Número da página (padrão: 0, primeira página)
     * @return Resposta HTTP 200 com a página de produtos no formato DTO
     * @throws ProductInvalidRequestException se os parâmetros de paginação forem inválidos
     */
    @GetMapping("/all")
    public ResponseEntity<ProductPageDTO> findAll(
            @RequestParam(defaultValue = "10") @Positive int size,
            @RequestParam(defaultValue = "0") @Min(0) int page) {
        logger.info("Recebida requisição para buscar todos os produtos. Página: {}, Tamanho: {}", page, size);
        ProductPageDTO products = productService.findAllProducts(page, size);
        return ResponseEntity.ok(products);
    }

    /**
     * Busca um produto específico pelo seu ID.
     *
     * @param productId ID do produto a ser buscado
     * @return Resposta HTTP 200 com o DTO do produto encontrado
     * @throws ProductNotFoundException se o produto não for encontrado
     * @throws ProductInvalidRequestException se o ID for inválido
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long productId) {
        logger.info("Recebida requisição para buscar produto com ID: {}", productId);
        ProductResponseDTO product = productService.findById(productId);
        return ResponseEntity.ok(product);
    }

    /**
     * Atualiza um produto existente com novos dados.
     *
     * @param productId ID do produto a ser atualizado
     * @param productCreationDTO Novos dados do produto
     * @return Resposta HTTP 200 com o DTO do produto atualizado
     * @throws ProductNotFoundException se o produto não for encontrado
     * @throws ProductInvalidRequestException se os parâmetros forem inválidos
     * @throws ProductConflictException se houver conflito de dados
     */
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long productId,
            @RequestBody ProductCreationDTO productCreationDTO) {
        logger.info("Recebida requisição para atualizar produto com ID: {}", productId);
        ProductResponseDTO updatedProduct = productService.updateProduct(productId, productCreationDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Desativa um produto existente, marcando-o como inativo.
     *
     * @param productId ID do produto a ser desativado
     * @return Resposta HTTP 200 com o DTO do produto desativado
     * @throws ProductNotFoundException se o produto não for encontrado
     * @throws ProductProcessingException se houver falha ao desativar
     */
    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<ProductResponseDTO> deactivate(@PathVariable Long productId) {
        logger.info("Recebida requisição para desativar produto com ID: {}", productId);
        Product deactivatedProduct = productService.delete(productId);
        return ResponseEntity.ok(productMapper.toResponseDTO(deactivatedProduct));
    }
}