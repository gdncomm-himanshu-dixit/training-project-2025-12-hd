package com.member.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginRequestDTO {

    /*** Req Mapping: Customer Login */


    @NotBlank(message = "Username cannot be empty")
    private String userName;

    @NotBlank(message = "Password cannot be empty")
    private String password;
    }


