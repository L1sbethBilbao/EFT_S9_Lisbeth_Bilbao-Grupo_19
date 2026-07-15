package com.minimarket.security.util;

import com.minimarket.security.config.JwtProperties;
import com.minimarket.security.config.MfaProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class JwtUtilTest {

    private static final String SECRET = "this-is-a-test-secret-key-32chars!!";

    private JwtProperties jwtProperties;
    private MfaProperties mfaProperties;
    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(SECRET);
        jwtProperties.setAccessExpiration(900_000L);
        jwtProperties.setRefreshExpiration(604_800_000L);

        mfaProperties = new MfaProperties();
        mfaProperties.setTokenExpirationMs(300_000L);

        jwtUtil = new JwtUtil(jwtProperties, mfaProperties);

        userDetails = User.builder()
                .username("cliente1")
                .password("x")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .build();
    }

    @Test
    @DisplayName("GIVEN Secreto Null O Corto WHEN Constructor THEN Usa Clave Padded")
    void givenSecretoNullOCorto_whenConstructor_thenUsaClavePadded() {
        JwtProperties nullSecret = new JwtProperties();
        nullSecret.setSecret(null);
        nullSecret.setAccessExpiration(900_000L);
        JwtUtil utilNull = new JwtUtil(nullSecret, mfaProperties);
        String token = utilNull.generateToken("user");
        assertThat(utilNull.extractUsername(token)).isEqualTo("user");

        JwtProperties shortSecret = new JwtProperties();
        shortSecret.setSecret("corto");
        shortSecret.setAccessExpiration(900_000L);
        JwtUtil utilShort = new JwtUtil(shortSecret, mfaProperties);
        assertThat(utilShort.extractUsername(utilShort.generateToken("user2"))).isEqualTo("user2");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Generate Access Token THEN Es Token De Tipo Access")
    void givenDefaultContext_whenGenerateAccessToken_thenEsTokenDeTipoAccess() {
        String token = jwtUtil.generateAccessToken(userDetails);

        assertThat(jwtUtil.isAccessToken(token)).isTrue();
        assertThat(jwtUtil.isRefreshToken(token)).isFalse();
        assertThat(jwtUtil.isMfaToken(token)).isFalse();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("cliente1");
        assertThat(jwtUtil.validateAccessToken(token, "cliente1")).isTrue();
        List<?> roles = jwtUtil.getClaimFromToken(token, c -> c.get(JwtUtil.CLAIM_ROLES, List.class));
        assertThat(roles).isEqualTo(List.of("ROLE_CLIENTE"));
    }

    @Test
    @DisplayName("GIVEN Username WHEN Generate Token THEN Genera Access Token")
    void givenUsername_whenGenerateToken_thenGeneraAccessToken() {
        String token = jwtUtil.generateToken("empleado1");

        assertThat(jwtUtil.isAccessToken(token)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("empleado1");
    }

    @Test
    @DisplayName("GIVEN User Details WHEN Generate Token THEN Delega En Access Token")
    void givenUserDetails_whenGenerateToken_thenDelegaEnAccessToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertThat(jwtUtil.validateAccessToken(token, "cliente1")).isTrue();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Generate Refresh Token THEN Es Token De Tipo Refresh")
    void givenDefaultContext_whenGenerateRefreshToken_thenEsTokenDeTipoRefresh() {
        String token = jwtUtil.generateRefreshToken("cliente1");

        assertThat(jwtUtil.isRefreshToken(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isFalse();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("cliente1");
        assertThat(jwtUtil.validateAccessToken(token, "cliente1")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Generate Refresh Token THEN Cada Emision Tiene Jti Distinto")
    void givenDefaultContext_whenGenerateRefreshToken_thenCadaEmisionTieneJtiDistinto() {
        String token1 = jwtUtil.generateRefreshToken("cliente1");
        String token2 = jwtUtil.generateRefreshToken("cliente1");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Generate Mfa Token THEN Es Token De Tipo Mfa")
    void givenDefaultContext_whenGenerateMfaToken_thenEsTokenDeTipoMfa() {
        String token = jwtUtil.generateMfaToken("gerente1");

        assertThat(jwtUtil.isMfaToken(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isFalse();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("gerente1");
    }

    @Test
    @DisplayName("GIVEN Sin Tipo Claim WHEN Is Access Token THEN Retorna True")
    void givenSinTipoClaim_whenIsAccessToken_thenRetornaTrue() {
        Map<String, Object> claims = new HashMap<>();
        String token = ReflectionTestUtils.invokeMethod(
                jwtUtil, "buildToken", claims, "legacy", 900_000L);

        assertThat(jwtUtil.getTokenType(token)).isNull();
        assertThat(jwtUtil.isAccessToken(token)).isTrue();
    }

    @Test
    @DisplayName("GIVEN Token Invalido WHEN Get Token Type THEN Retorna Null")
    void givenTokenInvalido_whenGetTokenType_thenRetornaNull() {
        assertThat(jwtUtil.getTokenType("token-invalido")).isNull();
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Extract Expiration THEN Retorna Fecha")
    void givenDefaultContext_whenExtractExpiration_thenRetornaFecha() {
        String token = jwtUtil.generateAccessToken(userDetails);

        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Incorrecto WHEN Validate Token THEN Retorna False")
    void givenUsuarioIncorrecto_whenValidateToken_thenRetornaFalse() {
        String token = jwtUtil.generateAccessToken(userDetails);

        assertThat(jwtUtil.validateToken(token, "otro")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Correcto Token Expirado WHEN Validate Token THEN Retorna False")
    void givenUsuarioCorrectoTokenExpirado_whenValidateToken_thenRetornaFalse() {
        String token = jwtUtil.generateAccessToken(userDetails);
        JwtUtil jwtUtilSpy = spy(jwtUtil);

        doReturn(true).when(jwtUtilSpy).isTokenExpired(token);

        assertThat(jwtUtilSpy.validateToken(token, "cliente1")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Incorrecto WHEN Validate Access Token THEN Retorna False")
    void givenUsuarioIncorrecto_whenValidateAccessToken_thenRetornaFalse() {
        String token = jwtUtil.generateAccessToken(userDetails);

        assertThat(jwtUtil.validateAccessToken(token, "otro")).isFalse();
    }

    @Test
    @DisplayName("GIVEN User Details WHEN Validate Token THEN Valida Username")
    void givenUserDetails_whenValidateToken_thenValidaUsername() {
        String token = jwtUtil.generateAccessToken(userDetails);

        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("GIVEN Expirado WHEN Validate Token THEN Retorna False")
    void givenExpirado_whenValidateToken_thenRetornaFalse() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret(SECRET);
        expiredProps.setAccessExpiration(-1000L);
        JwtUtil expiredUtil = new JwtUtil(expiredProps, mfaProperties);
        String token = expiredUtil.generateToken("cliente1");

        assertThatThrownBy(() -> expiredUtil.validateToken(token, "cliente1"))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("GIVEN Token Invalido WHEN Extract Username THEN Lanza Excepcion")
    void givenTokenInvalido_whenExtractUsername_thenLanzaExcepcion() {
        assertThatThrownBy(() -> jwtUtil.extractUsername("mal-formado"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Getters Expiracion THEN Retornan Valores Configurados")
    void givenDefaultContext_whenGettersExpiracion_thenRetornanValoresConfigurados() {
        assertThat(jwtUtil.getAccessExpiration()).isEqualTo(900_000L);
        assertThat(jwtUtil.getExpiration()).isEqualTo(900_000L);
        assertThat(jwtUtil.getRefreshExpiration()).isEqualTo(604_800_000L);
    }
}
