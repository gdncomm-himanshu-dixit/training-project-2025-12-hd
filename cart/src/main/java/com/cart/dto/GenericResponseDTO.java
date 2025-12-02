package com.cart.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenericResponseDTO<T> {
    private String status;
    private String message;
    private T data;
}
