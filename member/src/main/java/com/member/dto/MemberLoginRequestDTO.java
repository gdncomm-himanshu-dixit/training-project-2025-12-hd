package com.member.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoginRequestDTO {

    /*** Req Mapping: Customer Login */


        private String userName;
        private String password;
    }


