package com.minimarket.security.filter;

import com.minimarket.security.monitor.SuspiciousActivityService;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private SuspiciousActivityService suspiciousActivityService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        suspiciousActivityService.recordRequest(request);

        final String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (JwtException ex) {
                suspiciousActivityService.recordInvalidJwt(request, ex);
                log.warn("JWT inválido o expirado en {} ip={}: {}",
                        request.getRequestURI(), suspiciousActivityService.clientIp(request), ex.getMessage());
            }
        } else if (authHeader != null) {
            log.warn("Encabezado Authorization mal formado en {} ip={}",
                    request.getRequestURI(), suspiciousActivityService.clientIp(request));
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (!jwtUtil.isAccessToken(jwt)) {
                suspiciousActivityService.recordInvalidJwt(request, null);
                log.warn("Token no es de tipo access en {} ip={}",
                        request.getRequestURI(), suspiciousActivityService.clientIp(request));
            } else {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateAccessToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("JWT válido, usuario autenticado: {}", username);
                } else {
                    suspiciousActivityService.recordInvalidJwt(request, null);
                    log.warn("Validación JWT fallida para usuario {} en {}", username, request.getRequestURI());
                }
            }
        }

        chain.doFilter(request, response);
    }
}
