package com.minimarket.security.service;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.config.MfaProperties;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.model.MfaSetupResponse;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MfaProperties mfaProperties;

    @InjectMocks
    private MfaService mfaService;

    private Usuario gerente;
    private Usuario cliente;

    @BeforeEach
    void setUp() {
        Rol rolGerente = new Rol();
        rolGerente.setNombre(SecurityRoles.GERENTE);
        gerente = new Usuario();
        gerente.setUsername("gerente1");
        gerente.setRoles(Set.of(rolGerente));

        Rol rolCliente = new Rol();
        rolCliente.setNombre(SecurityRoles.CLIENTE);
        cliente = new Usuario();
        cliente.setUsername("cliente1");
        cliente.setRoles(Set.of(rolCliente));
    }

    @Test
    @DisplayName("GIVEN Usuario Null WHEN Is Gerente With Mfa THEN Retorna False")
    void givenUsuarioNull_whenIsGerenteWithMfa_thenRetornaFalse() {
        assertThat(mfaService.isGerenteWithMfa(null)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Mfa Deshabilitado WHEN Is Gerente With Mfa THEN Retorna False")
    void givenMfaDeshabilitado_whenIsGerenteWithMfa_thenRetornaFalse() {
        gerente.setMfaEnabled(false);

        assertThat(mfaService.isGerenteWithMfa(gerente)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Gerente Con Mfa WHEN Is Gerente With Mfa THEN Retorna True")
    void givenGerenteConMfa_whenIsGerenteWithMfa_thenRetornaTrue() {
        gerente.setMfaEnabled(true);

        assertThat(mfaService.isGerenteWithMfa(gerente)).isTrue();
    }

    @Test
    @DisplayName("GIVEN Cliente Con Mfa WHEN Is Gerente With Mfa THEN Retorna False")
    void givenClienteConMfa_whenIsGerenteWithMfa_thenRetornaFalse() {
        cliente.setMfaEnabled(true);

        assertThat(mfaService.isGerenteWithMfa(cliente)).isFalse();
    }

    @Test
    @DisplayName("GIVEN Gerente WHEN Setup Mfa THEN Genera Secreto Y Qr")
    void givenGerente_whenSetupMfa_thenGeneraSecretoYQr() {
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(java.util.Optional.of(gerente));
        when(mfaProperties.getIssuer()).thenReturn("MiniMarketPlus");
        when(usuarioRepository.save(gerente)).thenReturn(gerente);

        MfaSetupResponse response = mfaService.setupMfa("gerente1");

        assertThat(response.getSecret()).isNotBlank();
        assertThat(response.getQrUri()).isNotBlank();
        assertThat(gerente.getTotpSecret()).isEqualTo(response.getSecret());
        assertThat(gerente.isMfaEnabled()).isFalse();
    }

    @Test
    @DisplayName("GIVEN Usuario Inexistente WHEN Setup Mfa THEN Lanza Excepcion")
    void givenUsuarioInexistente_whenSetupMfa_thenLanzaExcepcion() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> mfaService.setupMfa("desconocido"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("GIVEN No Gerente WHEN Setup Mfa THEN Lanza Excepcion")
    void givenNoGerente_whenSetupMfa_thenLanzaExcepcion() {
        when(usuarioRepository.findByUsername("cliente1")).thenReturn(java.util.Optional.of(cliente));

        assertThatThrownBy(() -> mfaService.setupMfa("cliente1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MFA solo disponible");
    }

    @Test
    @DisplayName("GIVEN Usuario Inexistente WHEN Confirm Mfa THEN Lanza Excepcion")
    void givenUsuarioInexistente_whenConfirmMfa_thenLanzaExcepcion() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> mfaService.confirmMfa("desconocido", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    @Test
    @DisplayName("GIVEN Codigo Valido WHEN Confirm Mfa THEN Activa Mfa")
    void givenCodigoValido_whenConfirmMfa_thenActivaMfa() throws CodeGenerationException {
        String secret = "JBSWY3DPEHPK3PXP";
        gerente.setTotpSecret(secret);
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(java.util.Optional.of(gerente));
        when(usuarioRepository.save(gerente)).thenReturn(gerente);

        DefaultCodeGenerator generator = new DefaultCodeGenerator();
        String code = generator.generate(secret, Math.floorDiv(System.currentTimeMillis(), 30000L));

        mfaService.confirmMfa("gerente1", code);

        assertThat(gerente.isMfaEnabled()).isTrue();
        assertThat(gerente.getMfaEnrolledAt()).isNotNull();
    }

    @Test
    @DisplayName("GIVEN Codigo Invalido WHEN Confirm Mfa THEN Lanza Excepcion")
    void givenCodigoInvalido_whenConfirmMfa_thenLanzaExcepcion() {
        gerente.setTotpSecret("JBSWY3DPEHPK3PXP");
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(java.util.Optional.of(gerente));

        assertThatThrownBy(() -> mfaService.confirmMfa("gerente1", "000000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código TOTP inválido");
    }

    @Test
    @DisplayName("GIVEN Sin Secreto WHEN Confirm Mfa THEN Lanza Excepcion")
    void givenSinSecreto_whenConfirmMfa_thenLanzaExcepcion() {
        when(usuarioRepository.findByUsername("gerente1")).thenReturn(java.util.Optional.of(gerente));

        assertThatThrownBy(() -> mfaService.confirmMfa("gerente1", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Código TOTP inválido");
    }

    @Test
    @DisplayName("GIVEN Sin Secreto WHEN Verify Code THEN Retorna False")
    void givenSinSecreto_whenVerifyCode_thenRetornaFalse() {
        assertThat(mfaService.verifyCode(gerente, "123456")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Codigo Valido WHEN Verify Code THEN Retorna True")
    void givenCodigoValido_whenVerifyCode_thenRetornaTrue() throws CodeGenerationException {
        String secret = "JBSWY3DPEHPK3PXP";
        gerente.setTotpSecret(secret);
        DefaultCodeGenerator generator = new DefaultCodeGenerator();
        String code = generator.generate(secret, Math.floorDiv(System.currentTimeMillis(), 30000L));

        assertThat(mfaService.verifyCode(gerente, code)).isTrue();
    }

    @Test
    @DisplayName("GIVEN Codigo Invalido WHEN Verify Code THEN Retorna False")
    void givenCodigoInvalido_whenVerifyCode_thenRetornaFalse() {
        gerente.setTotpSecret("JBSWY3DPEHPK3PXP");

        assertThat(mfaService.verifyCode(gerente, "000000")).isFalse();
    }

    @Test
    @DisplayName("GIVEN Fallo Generacion WHEN Build Qr Uri THEN Usa Fallback Otpauth")
    void givenFalloGeneracion_whenBuildQrUri_thenUsaFallbackOtpauth() throws QrGenerationException {
        when(mfaProperties.getIssuer()).thenReturn("MiniMarketPlus");

        try (MockedConstruction<ZxingPngQrGenerator> ignored = mockConstruction(
                ZxingPngQrGenerator.class,
                (mock, context) -> {
                    when(mock.generate(any())).thenThrow(new QrGenerationException("error", new RuntimeException()));
                    when(mock.getImageMimeType()).thenReturn("image/png");
                })) {
            String uri = ReflectionTestUtils.invokeMethod(mfaService, "buildQrUri", "gerente1", "SECRET");

            assertThat(uri).contains("otpauth://totp/");
            assertThat(uri).contains("gerente1");
            assertThat(uri).contains("SECRET");
        }
    }
}
