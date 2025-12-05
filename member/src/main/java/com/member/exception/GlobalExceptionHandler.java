package com.member.exception;


import com.member.dto.GenericResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // 404 - Member not found (your custom exception)
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<GenericResponseDTO<?>> handleNotFound(MemberNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message(ex.getMessage())
                        .build());
    }

    // 400 - Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponseDTO<?>> handleValidation(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity.badRequest()
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message(errorMessage)
                        .build());
    }

    // 400 - Path variable / request param mismatch
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericResponseDTO<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message("Invalid value for parameter: " + ex.getName())
                        .build());
    }

    // 404 - Route not found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<GenericResponseDTO<?>> handleNoHandler(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message("Endpoint not found")
                        .build());
    }

    // 401 - Unauthorized (if Spring Security triggers)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<GenericResponseDTO<?>> handleAccessDenied(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message("Unauthorized access")
                        .build());
    }

    // 500 - Internal server error
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericResponseDTO<?>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message(ex.getMessage())
                        .build());
    }

    // 500 - Catch-all fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponseDTO<?>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GenericResponseDTO.builder()
                        .status("ERROR")
                        .message("Something went wrong")
                        .build());
    }


}
