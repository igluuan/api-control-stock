package tech.devluan.api_control_stock.exceptions;

public class ProductInvalidDataException extends RuntimeException {
    public ProductInvalidDataException(String message) {
        super(message);
    }
}
