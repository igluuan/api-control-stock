package tech.devluan.api_control_stock.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

/**
 * Classe global para tratamento de exceções em toda a aplicação.
 * Centraliza a lógica de resposta para erros específicos, retornando respostas HTTP apropriadas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Record para respostas de erro estruturadas
    public record ErrorResponse(String errorCode, String message, List<String> details) {}

    /**
     * Trata exceções de validação de argumentos (Bean Validation).
     *
     * @param ex Exceção lançada quando a validação de um argumento falha
     * @return Resposta HTTP 400 com detalhes dos erros de validação
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Campo '%s': %s", error.getField(), error.getDefaultMessage()))
                .toList();
        logger.error("Erro de validação: {}", details);
        ErrorResponse error = new ErrorResponse("VALIDATION_FAILED", "Dados inválidos fornecidos", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Trata exceções de recurso não encontrado.
     *
     * @param ex Exceção lançada quando um produto não é encontrado
     * @return Resposta HTTP 404 com a mensagem de erro
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException ex) {
        logger.error("Produto não encontrado: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata exceções de requisições inválidas.
     *
     * @param ex Exceção lançada quando os parâmetros da requisição são inválidos
     * @return Resposta HTTP 400 com a mensagem de erro
     */
    @ExceptionHandler(ProductInvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(ProductInvalidRequestException ex) {
        logger.error("Requisição inválida: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("INVALID_REQUEST", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Trata exceções de conflitos de dados.
     *
     * @param ex Exceção lançada quando há conflito de dados (ex.: duplicidade)
     * @return Resposta HTTP 409 com a mensagem de erro
     */
    @ExceptionHandler(ProductConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ProductConflictException ex) {
        logger.error("Conflito de dados: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("CONFLICT", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Trata exceções de falhas no processamento.
     *
     * @param ex Exceção lançada quando ocorre um erro interno no processamento
     * @return Resposta HTTP 500 com a mensagem de erro
     */
    @ExceptionHandler(ProductProcessingException.class)
    public ResponseEntity<ErrorResponse> handleProcessingError(ProductProcessingException ex) {
        logger.error("Erro interno ao processar requisição: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("PROCESSING_ERROR", ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Trata exceções genéricas não esperadas, incluindo erros do Swagger.
     *
     * @param ex Exceção genérica capturada como fallback
     * @return Resposta HTTP 500 com uma mensagem genérica
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Erro inesperado: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse("INTERNAL_ERROR",
                "Ocorreu um erro interno no servidor. Por favor, tente novamente mais tarde.",
                List.of(ex.getClass().getSimpleName() + ": " + ex.getMessage()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}