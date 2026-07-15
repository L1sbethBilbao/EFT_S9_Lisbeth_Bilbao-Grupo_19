package com.minimarket.security.util;

import com.minimarket.security.config.JwtProperties;
import com.minimarket.security.config.MfaProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_TOKEN_TYPE = "type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";
    public static final String TOKEN_TYPE_MFA = "mfa";

    private final JwtProperties props;
    private final MfaProperties mfaProperties;
    private final SecretKey key;

    @Autowired
    public JwtUtil(JwtProperties props, MfaProperties mfaProperties) {
        this.props = props;
        this.mfaProperties = mfaProperties;
        byte[] keyBytes = props.getSecret() != null
                ? props.getSecret().getBytes(StandardCharsets.UTF_8)
                : new byte[0];
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put(CLAIM_ROLES, roles);
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(claims, userDetails.getUsername(), props.getAccessExpiration());
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH);
        return buildToken(claims, username, props.getRefreshExpiration());
    }

    /** @deprecated use {@link #generateAccessToken(UserDetails)} */
    public String generateToken(UserDetails userDetails) {
        return generateAccessToken(userDetails);
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS);
        return buildToken(claims, username, props.getAccessExpiration());
    }

    public String generateMfaToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_TOKEN_TYPE, TOKEN_TYPE_MFA);
        return buildToken(claims, username, mfaProperties.getTokenExpirationMs());
    }

    private String buildToken(Map<String, Object> claims, String subject, long ttlMs) {
        long now = System.currentTimeMillis();
        claims.put("jti", UUID.randomUUID().toString());
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMs))
                .signWith(key)
                .compact();
    }

    public boolean isMfaToken(String token) {
        return TOKEN_TYPE_MFA.equals(getTokenType(token));
    }

    public boolean isAccessToken(String token) {
        String type = getTokenType(token);
        return type == null || TOKEN_TYPE_ACCESS.equals(type);
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    public String getTokenType(String token) {
        try {
            return getClaimFromToken(token, claims -> (String) claims.get(CLAIM_TOKEN_TYPE));
        } catch (JwtException e) {
            return null;
        }
    }

    public String extractUsername(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return username.equals(tokenUsername) && !isTokenExpired(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails.getUsername());
    }

    public boolean validateAccessToken(String token, String username) {
        return isAccessToken(token) && validateToken(token, username);
    }

    public long getAccessExpiration() {
        return props.getAccessExpiration();
    }

    /** @deprecated use {@link #getAccessExpiration()} */
    public long getExpiration() {
        return getAccessExpiration();
    }

    public long getRefreshExpiration() {
        return props.getRefreshExpiration();
    }
}
