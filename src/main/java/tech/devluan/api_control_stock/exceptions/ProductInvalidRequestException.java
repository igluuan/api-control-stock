package tech.devluan.api_control_stock.exceptions;

public class ProductInvalidRequestException extends RuntimeException {
    public ProductInvalidRequestException(String message) {
        super(message);
    }

    public ProductInvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
