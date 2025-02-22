package tech.devluan.api_control_stock.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tech.devluan.api_control_stock.exceptions.*;
import tech.devluan.api_control_stock.model.Product;
import tech.devluan.api_control_stock.model.dto.ProductCreationDTO;
import tech.devluan.api_control_stock.model.dto.ProductPageDTO;
import tech.devluan.api_control_stock.model.dto.ProductResponseDTO;
import tech.devluan.api_control_stock.repository.ProductRepository;
import tech.devluan.api_control_stock.service.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Serviço responsável por gerenciar operações relacionadas a produtos, como criação, busca e atualização.
 * Todas as operações são transacionais para garantir consistência nos dados.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    /**
     * Cria um novo produto no sistema com base nos dados fornecidos.
     * Todos os campos do DTO são obrigatórios para a criação.
     *
     * @param productCreationDTO Objeto contendo as informações do novo produto (nome, preço e quantidade)
     * @return DTO com os dados do produto criado, incluindo o ID gerado
     * @throws ProductInvalidDataException se algum campo obrigatório estiver nulo
     * @throws ProductConflictException se houver conflito de dados no banco (ex.: nome duplicado)
     * @throws ProductProcessingException se ocorrer um erro inesperado durante o processamento
     */
    public ProductResponseDTO createNewProduct(ProductCreationDTO productCreationDTO) {
        if (productCreationDTO.name() == null
                && productCreationDTO.price() == null
                && productCreationDTO.quantity() == null) {
            logger.warn("Todos os campos são necessários para a criação de um novo produto!");
            throw new ProductInvalidDataException("Preencha todos os campos obrigatórios");
        }
        logger.info("Criando um novo produto: {}", productCreationDTO.name());
        try {
            Product newProduct = productMapper.toEntity(productCreationDTO);
            productRepository.save(newProduct);
            logger.info("Produto criado com sucesso! Identificador do produto: {}", newProduct.getProductId());
            return productMapper.toResponseDTO(newProduct);
        } catch (DataIntegrityViolationException e) {
            logger.error("Erro de integridade ao criar o produto: {}", e.getMessage());
            throw new ProductConflictException("Conflito de dados ao criar o produto", e);
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar o produto: {}", e.getMessage());
            throw new ProductProcessingException("Falha ao processar a criação do produto", e);
        }
    }

    /**
     * Busca todos os produtos cadastrados no sistema com suporte a paginação.
     * Retorna uma página de produtos conforme os parâmetros de página e tamanho fornecidos.
     *
     * @param page Número da página a ser retornada (começando em 0)
     * @param size Quantidade de produtos por página
     * @return Objeto ProductPageDTO contendo a lista paginada de produtos no formato de resposta
     * @throws ProductInvalidRequestException se os parâmetros de paginação (page ou size) forem inválidos, como negativos ou excessivamente grandes
     * @throws ProductProcessingException se ocorrer um erro inesperado durante a busca ou mapeamento dos produtos
     */
    public ProductPageDTO findAllProducts(int page, int size) {
        if (page < 0) {
            logger.error("Parâmetros de paginação inválidos: page={}, size={}", page, size);
            throw new ProductInvalidRequestException("O número da página deve ser não negativo");
        }
        if (size <= 0) {
            logger.error("Parâmetros de paginação inválidos: page={}, size={}", page, size);
            throw new ProductInvalidRequestException("O tamanho da página deve ser maior que zero");
        }
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Product> productPage = productRepository.findAll(pageRequest);
            logger.info("Buscando todos os produtos.");
            Page<ProductResponseDTO> dtoPage = productMapper.toPageDTO(productPage);
            logger.info("Sucesso ao buscar todos os produtos!");
            return ProductPageDTO.from(dtoPage);
        } catch (Exception e) {
            logger.error("Erro ao buscar produtos: {}", e.getMessage());
            throw new ProductProcessingException("Falha ao processar a busca de produtos", e);
        }
    }

    /**
     * Busca um produto específico pelo seu identificador único.
     *
     * @param productId ID do produto a ser buscado
     * @return DTO contendo as informações do produto encontrado
     * @throws ProductInvalidRequestException se o ID for nulo
     * @throws ProductNotFoundException se nenhum produto for encontrado com o ID informado
     * @throws ProductProcessingException se ocorrer um erro inesperado ao buscar o produto
     */
    public ProductResponseDTO findById(Long productId) {
        if (productId == null) {
            logger.error("ID não enviado.");
            throw new ProductInvalidRequestException("ID nulo, envie um ID válido. Ex: 1");
        }
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com ID: " + productId));
            logger.info("Buscando informações sobre o produto: {}", product.getProductId());
            return productMapper.toResponseDTO(product);
        } catch (Exception e) {
            logger.error("Erro ao buscar o produto com identificador: {}", productId);
            throw new ProductProcessingException("Falha ao processar a busca do produto", e);
        }
    }

    /**
     * Atualiza as informações de um produto existente no sistema.
     * O produto é identificado pelo ID e atualizado com os dados do DTO fornecido.
     *
     * @param productId ID do produto a ser atualizado
     * @param productCreationDTO Objeto contendo os novos dados do produto (nome, preço e quantidade)
     * @return DTO com as informações atualizadas do produto
     * @throws ProductInvalidRequestException se o ID ou o DTO for nulo
     * @throws ProductNotFoundException se o produto não for encontrado com o ID informado
     * @throws ProductConflictException se houver conflito de dados ao salvar (ex.: violação de constraint)
     * @throws ProductProcessingException se ocorrer um erro inesperado durante a atualização
     */
    public ProductResponseDTO updateProduct(Long productId, ProductCreationDTO productCreationDTO) {
        if (productId == null) {
            throw new ProductInvalidRequestException("O ID do produto não pode ser nulo");
        }
        if (productCreationDTO == null) {
            throw new ProductInvalidRequestException("O DTO de criação do produto não pode ser nulo");
        }

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com ID: " + productId));

            updateProductFields(product, productCreationDTO);

            try {
                Product updatedProduct = productRepository.save(product);
                logger.info("Produto atualizado com sucesso: {}", productId);
                return productMapper.toResponseDTO(updatedProduct);
            } catch (Exception e) {
                logger.error("Erro ao atualizar o produto: {}", e.getMessage());
                throw new ProductProcessingException("Falha ao processar a atualização do produto", e);
            }
        } catch (ProductNotFoundException e) {
            logger.error("Produto não encontrado: {}", e.getMessage());
            throw new ProductProcessingException("Falha ao processar a atualização do produto", e);
        }
    }

    /**
     * Desativa um produto existente no sistema, alterando sua situação para inativo.
     * O produto não é removido fisicamente do banco de dados, apenas marcado como inativo.
     *
     * @param productId ID do produto a ser desativado
     * @return Entidade Product atualizada com a situação inativo
     * @throws ProductNotFoundException se nenhum produto for encontrado com o ID informado
     * @throws ProductProcessingException se ocorrer um erro inesperado ao atualizar a situação do produto
     */
    public Product delete(Long productId) {
        if (productId == null) {
            throw new ProductInvalidRequestException("O ID do produto não pode ser nulo");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado com ID: " + productId));

        try {
            product.setActive(false);
            Product updatedProduct = productRepository.save(product);
            logger.info("Produto desativado com sucesso. ID: {}", productId);
            return updatedProduct;
        } catch (Exception e) {
            logger.error("Erro ao desativar o produto com ID {}: {}", productId, e.getMessage());
            throw new ProductProcessingException("Falha ao processar a desativação do produto", e);
        }
    }

    // Atualiza os campos do produto
    private void updateProductFields(Product product, ProductCreationDTO dto) {
        if (dto.name() != null) {
            product.setProductName(dto.name());
        }
        if (dto.price() != null) {
            product.setProductPrice(validatePrice(dto.price()));
        }
        if (dto.quantity() != null) {
            product.setProductQuantity(validateQuantity(dto.quantity()));
        }
        product.setUpdatedAt(LocalDateTime.now());
    }

    private Integer validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new ProductInvalidDataException("A quantidade deve ser não negativa");
        }
        return quantity;
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductInvalidDataException("O preço deve ser não negativo");
        }
        return price;
    }
}