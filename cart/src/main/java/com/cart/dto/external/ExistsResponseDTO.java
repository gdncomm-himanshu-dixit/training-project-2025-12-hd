package com.cart.dto.external;

import lombok.Data;

@Data
public class ExistsResponseDTO {
    private String status;
    private String message;
    private Boolean data;
}