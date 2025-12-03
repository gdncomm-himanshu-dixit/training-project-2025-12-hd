package com.member.serviceImpl;


import com.member.dto.MemberLoginRequestDTO;
import com.member.dto.MemberRegisterRequestDTO;
import com.member.dto.MemberResponse;
import com.member.entity.MemberEntity;
import com.member.exception.MemberNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.member.repositories.MemberRepository;
import com.member.services.MemberService;

@Slf4j
@Service
@Data
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberServiceImpl(MemberRepository memberRepository,
                             PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Register User + Hash Password using Spring Security encoder */
    @Override
    public MemberResponse register(MemberRegisterRequestDTO request) {
        log.info("Registering new member: {}", request.getUserName());

        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        MemberEntity member = MemberEntity.builder()
                .userName(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))  // hashing
                .email(request.getEmail())
                .isActive(true)
                .build();

        memberRepository.save(member);

        return MemberResponse.builder()
                .userName(member.getUserName())
                .email(member.getEmail())
                .active(true)
                .build();
    }

    /** Login user + Validate password with Spring Security PasswordEncoder*/
    @Override
    public MemberResponse login(MemberLoginRequestDTO request) {
        log.info("Login request: {}", request.getUserName());

        MemberEntity member = memberRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new MemberNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return MemberResponse.builder()
                .userName(member.getUserName())
                .email(member.getEmail())
                .active(member.getIsActive())
                .build();
    }
}
