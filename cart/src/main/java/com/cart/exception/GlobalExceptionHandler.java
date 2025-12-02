package com.cart.exception;


import com.cart.dto.GenericResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CartNotFoundException.class)
    public GenericResponseDTO<?> handleCart(CartNotFoundException ex) {
        return GenericResponseDTO.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .data(null)
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    public GenericResponseDTO<?> handleRuntime(RuntimeException ex) {
        return GenericResponseDTO.builder()
                .status("ERROR")
                .message(ex.getMessage())
                .build();
    }
}

