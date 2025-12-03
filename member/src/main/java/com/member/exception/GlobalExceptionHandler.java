package com.member.exception;


import com.member.dto.GenericResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public GenericResponseDTO<?> handleNotFound(MemberNotFoundException ex) {
        return GenericResponseDTO.builder()
                .status("ERROR")
                .message(ex.getMessage())
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
