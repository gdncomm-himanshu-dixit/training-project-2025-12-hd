package com.member.dto;


import lombok.*;

/**
 * Req Mapping: Customer registration payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegisterRequestDTO {
    private String userName;
    private String password;
    private String email;
}
