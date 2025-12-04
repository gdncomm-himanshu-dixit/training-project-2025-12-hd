package com.member.dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private String userName;
    private String email;
    private boolean active;
    private String token;
}
