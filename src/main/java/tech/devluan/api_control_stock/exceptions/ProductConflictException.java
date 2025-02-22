package tech.devluan.api_control_stock.exceptions;

public class ProductConflictException extends RuntimeException {
    public ProductConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
