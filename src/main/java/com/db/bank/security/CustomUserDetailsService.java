package com.db.bank.security;

import com.db.bank.domain.entity.User;
import com.db.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByUserId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("로그인 아이디를 찾을 수 없습니다: " + loginId));

        return new CustomUserDetails(user);
    }
}
