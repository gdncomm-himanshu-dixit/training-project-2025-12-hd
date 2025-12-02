package com.product.exception;

import com.product.dto.GenericResponse;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public GenericResponse<?> handleNotFound(ProductNotFoundException ex) {
        return GenericResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    public GenericResponse<?> handleRuntime(RuntimeException ex) {
        return GenericResponse.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .build();
    }
}
