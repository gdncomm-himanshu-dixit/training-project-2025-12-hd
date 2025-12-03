package com.member.services;


import com.member.dto.MemberLoginRequestDTO;
import com.member.dto.MemberRegisterRequestDTO;
import com.member.dto.MemberResponse;

/**
     * Req Mapping:
     * - Register user
     * - Login user (password validation)
     */
    public interface MemberService {

        MemberResponse register(MemberRegisterRequestDTO request);

        MemberResponse login(MemberLoginRequestDTO request);
    }


