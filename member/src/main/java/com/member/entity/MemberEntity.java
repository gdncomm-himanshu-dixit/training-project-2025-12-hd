package com.member.entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Requirement Mapping:
 * - Member entity storing user credentials (Req: customer register/login)
 * - Password must be hashed (Req: use Spring built-in encoder)
 */
@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userName;
    private String password;   // hashed password stored
    private String email;
    private Boolean isActive;
}
