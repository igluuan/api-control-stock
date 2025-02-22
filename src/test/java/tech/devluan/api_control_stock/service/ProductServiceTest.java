package tech.devluan.api_control_stock.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import tech.devluan.api_control_stock.exceptions.*;
import tech.devluan.api_control_stock.model.Product;
import tech.devluan.api_control_stock.model.dto.ProductCreationDTO;
import tech.devluan.api_control_stock.model.dto.ProductPageDTO;
import tech.devluan.api_control_stock.model.dto.ProductResponseDTO;
import tech.devluan.api_control_stock.repository.ProductRepository;
import tech.devluan.api_control_stock.service.mapper.ProductMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductCreationDTO creationDTO;
    private ProductResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        product = new Product(1L, "Toner 5112", 10, BigDecimal.valueOf(59.90),
                LocalDateTime.now(), LocalDateTime.now(), true);
        creationDTO = new ProductCreationDTO("Toner 5112", 10, BigDecimal.valueOf(59.90));
        responseDTO = new ProductResponseDTO("Toner 5112", 10, BigDecimal.valueOf(59.90),
                LocalDateTime.now());
    }

    @Test
    void createNewProduct_success() {
        when(productMapper.toEntity(creationDTO)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO result = productService.createNewProduct(creationDTO);

        assertNotNull(result);
        assertEquals("Toner 5112", result.name());
        assertEquals(10, result.quantity());
        assertEquals(BigDecimal.valueOf(59.90), result.price());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createNewProduct_nullFields_throwsException() {
        ProductCreationDTO invalidDTO = new ProductCreationDTO(null, null, null);

        ProductInvalidDataException exception = assertThrows(ProductInvalidDataException.class,
                () -> productService.createNewProduct(invalidDTO));
        assertEquals("Preencha todos os campos obrigatórios", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createNewProduct_dataIntegrityViolation_throwsConflictException() {
        when(productMapper.toEntity(creationDTO)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        ProductConflictException exception = assertThrows(ProductConflictException.class,
                () -> productService.createNewProduct(creationDTO));
        assertEquals("Conflito de dados ao criar o produto", exception.getMessage());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void findAllProducts_success() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);
        Page<ProductResponseDTO> dtoPage = new PageImpl<>(List.of(responseDTO), pageRequest, 1);

        when(productRepository.findAll(pageRequest)).thenReturn(productPage);
        when(productMapper.toPageDTO(productPage)).thenReturn(dtoPage);

        ProductPageDTO result = productService.findAllProducts(0, 10);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals(0, result.currentPage());
        assertEquals(1, result.totalPages());
        assertEquals(1, result.products().size());
        assertEquals("Toner 5112", result.products().get(0).name());
        verify(productRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void findAllProducts_invalidPagination_throwsException() {
        // Don't set up mock behavior that would trigger IllegalArgumentException
        ProductInvalidRequestException exception = assertThrows(ProductInvalidRequestException.class,
                () -> productService.findAllProducts(-1, 10));

        assertEquals("Parâmetros de página ou tamanho inválidos", exception.getMessage());
        // Since the validation fails before repository is called, verify it was never called
        verify(productRepository, never()).findAll(any(PageRequest.class));
    }
    @Test
    void findById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponseDTO(product)).thenReturn(responseDTO);

        ProductResponseDTO result = productService.findById(1L);

        assertNotNull(result);
        assertEquals("Toner 5112", result.name());
        assertEquals(10, result.quantity());
        assertEquals(BigDecimal.valueOf(59.90), result.price());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void findById_notFound_throwsProcessingException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductProcessingException exception = assertThrows(ProductProcessingException.class,
                () -> productService.findById(999L));
        assertEquals("Falha ao processar a busca do produto", exception.getMessage());
        assertTrue(exception.getCause() instanceof ProductNotFoundException);
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void findById_nullId_throwsException() {
        ProductInvalidRequestException exception = assertThrows(ProductInvalidRequestException.class,
                () -> productService.findById(null));
        assertEquals("ID nulo, envie um ID válido. Ex: 1", exception.getMessage());
        verify(productRepository, never()).findById(any());
    }

    @Test
    void updateProduct_success_partialUpdate() {
        ProductCreationDTO updateDTO = new ProductCreationDTO(null, 20, BigDecimal.valueOf(99.90));
        Product updatedProduct = new Product(1L, "Toner 5112", 20, BigDecimal.valueOf(99.90),
                product.getCreatedAt(), LocalDateTime.now(), true);
        ProductResponseDTO updatedResponseDTO = new ProductResponseDTO("Toner 5112", 20,
                BigDecimal.valueOf(99.90), LocalDateTime.now());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toResponseDTO(updatedProduct)).thenReturn(updatedResponseDTO);

        ProductResponseDTO result = productService.updateProduct(1L, updateDTO);

        assertNotNull(result);
        assertEquals("Toner 5112", result.name());
        assertEquals(20, result.quantity());
        assertEquals(BigDecimal.valueOf(99.90), result.price());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_notFound_throwsProcessingException() {
        ProductCreationDTO updateDTO = new ProductCreationDTO("Toner Atualizado", 20, BigDecimal.valueOf(99.90));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductProcessingException exception = assertThrows(ProductProcessingException.class,
                () -> productService.updateProduct(999L, updateDTO));
        assertEquals("Falha ao processar a atualização do produto", exception.getMessage());
        assertTrue(exception.getCause() instanceof ProductNotFoundException);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_nullDTO_throwsException() {
        ProductInvalidRequestException exception = assertThrows(ProductInvalidRequestException.class,
                () -> productService.updateProduct(1L, null));
        assertEquals("O DTO de criação do produto não pode ser nulo", exception.getMessage());
        verify(productRepository, never()).findById(any());
    }

    @Test
    void delete_success() {
        Product deactivatedProduct = new Product(1L, "Toner 5112", 10, BigDecimal.valueOf(59.90),
                product.getCreatedAt(), LocalDateTime.now(), false);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(deactivatedProduct);

        Product result = productService.delete(1L);

        assertNotNull(result);
        assertFalse(result.isActive());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void delete_notFound_throwsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
                () -> productService.delete(999L));
        assertEquals("Produto não encontrado com ID: 999", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void delete_nullId_throwsException() {
        ProductInvalidRequestException exception = assertThrows(ProductInvalidRequestException.class,
                () -> productService.delete(null));
        assertEquals("O ID do produto não pode ser nulo", exception.getMessage());
        verify(productRepository, never()).findById(any());
    }

    @Test
    void updateProduct_negativeQuantity_throwsException() {
        ProductCreationDTO updateDTO = new ProductCreationDTO(null, -5, BigDecimal.valueOf(99.90));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductInvalidDataException exception = assertThrows(ProductInvalidDataException.class,
                () -> productService.updateProduct(1L, updateDTO));
        assertEquals("A quantidade deve ser não negativa", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_negativePrice_throwsException() {
        ProductCreationDTO updateDTO = new ProductCreationDTO(null, 20, BigDecimal.valueOf(-99.90));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        ProductInvalidDataException exception = assertThrows(ProductInvalidDataException.class,
                () -> productService.updateProduct(1L, updateDTO));
        assertEquals("O preço deve ser não negativo", exception.getMessage());
        verify(productRepository, never()).save(any());
    }
}