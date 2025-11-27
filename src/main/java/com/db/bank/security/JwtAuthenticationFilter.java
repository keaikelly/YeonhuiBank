package com.db.bank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String bearerToken = request.getHeader("Authorization");

        // 🔎 1) 요청 URI + 토큰 로그
        log.info("[JWT] URI = {}, Authorization = {}", request.getRequestURI(), bearerToken);

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            // 토큰 아예 없으면 그냥 다음 필터로
            filterChain.doFilter(request, response);
            return;
        }

        String token = bearerToken.substring(7);
        log.info("[JWT] raw token = {}", token);

        // 이미 인증된 상태면 또 할 필요 없음
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (jwtTokenProvider.validateToken(token)) {
            String loginId = jwtTokenProvider.getLoginId(token);
            log.info("[JWT] token valid, loginId = {}", loginId);

            CustomUserDetails userDetails =
                    (CustomUserDetails) userDetailsService.loadUserByUsername(loginId);

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);

            // 🔎 2) 최종 Authentication 확인
            log.info("[JWT] Authentication set = {}", auth);
        } else {
            log.info("[JWT] token invalid");
        }

        filterChain.doFilter(request, response);
    }
}
