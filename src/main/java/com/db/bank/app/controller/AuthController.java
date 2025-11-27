package com.db.bank.app.controller;

import com.db.bank.apiPayload.ApiResponse;
import com.db.bank.apiPayload.Status;
import com.db.bank.apiPayload.exception.UserException;
import com.db.bank.app.dto.AuthDto;
import com.db.bank.domain.entity.User;
import com.db.bank.repository.UserRepository;
import com.db.bank.security.CustomUserDetails;
import com.db.bank.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody AuthDto.SignupRequest req) {

        if (userRepository.existsByUserId(req.getUserId())) {
            // 네가 만든 Custom Exception 던져도 됨
            throw new UserException.UserAlreadyExistsException("이미 존재하는 ID"+req.getUserId());
        }

        User user = User.builder()
                .name(req.getName())
                .userId(req.getUserId())
                .password(passwordEncoder.encode(req.getPassword()))
                .build();

        userRepository.save(user);

        return ApiResponse.onSuccess(Status.USER_SIGNUP_SUCCESS, "회원가입 완료");
    }

    @PostMapping("/login")
    public ApiResponse<AuthDto.LoginResponse> login(@RequestBody AuthDto.LoginRequest req) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.getUserId(), req.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();

        String token = jwtTokenProvider.createToken(
                principal.getUsername(),  // loginId
                principal.getId()         // PK
        );

        AuthDto.LoginResponse body = AuthDto.LoginResponse.builder()
                .token(token)
                .userPk(principal.getId())
                .userId(principal.getUsername())
                .name(principal.getUsername())
                .build();

        return ApiResponse.onSuccess(Status.USER_LOGIN_SUCCESS, body);
    }
}
