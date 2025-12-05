package com.member.controller;
import com.member.dto.GenericResponseDTO;
import com.member.dto.MemberLoginRequestDTO;
import com.member.dto.MemberRegisterRequestDTO;
import com.member.dto.MemberResponse;
import com.member.repositories.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import com.member.services.MemberService;

import java.util.UUID;

/**
 * Req:
 * - Register
 * - Login (returns user info, Gateway will generate JWT)
 */

@RestController
@Slf4j
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;  // <-- INJECT REPOSITORY

    private final MemberService memberService;

//    Keeping it commented for testing @RequiredArgsConstructor annotation
//    public MemberController(MemberService memberService) {
//        this.memberService = memberService;
//    }
//


    /** Customer Registration */
    @PostMapping("/register")
    public GenericResponseDTO<MemberResponse> register(@RequestBody MemberRegisterRequestDTO request) {
        log.info("Member registration initiated");
        return GenericResponseDTO.<MemberResponse>builder()
                .status("SUCCESS")
                .message("Member registered")
                .data(memberService.register(request))
                .build();
    }

    /** Customer Login */
    @PostMapping("/login")
    public GenericResponseDTO<MemberResponse> login(@Valid @RequestBody MemberLoginRequestDTO request) {
        log.info("Member login initiated");
        return GenericResponseDTO.<MemberResponse>builder()
                .status("SUCCESS")
                .message("Login successful")
                .data(memberService.login(request))
                .build();
    }

    @GetMapping("/exists/{id}")
    public GenericResponseDTO<Boolean> existsById(@PathVariable UUID id) {

        boolean exists = memberRepository.existsById(id);

        return GenericResponseDTO.<Boolean>builder()
                .status("SUCCESS")
                .message("Lookup completed")
                .data(exists)
                .build();
    }

}
