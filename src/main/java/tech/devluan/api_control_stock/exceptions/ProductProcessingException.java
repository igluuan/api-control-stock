package tech.devluan.api_control_stock.exceptions;

public class ProductProcessingException extends RuntimeException {
    public ProductProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
