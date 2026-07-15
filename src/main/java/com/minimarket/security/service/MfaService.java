package com.minimarket.security.service;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.config.MfaProperties;
import com.minimarket.security.constants.SecurityRoles;
import com.minimarket.security.model.MfaSetupResponse;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class MfaService {

    private final UsuarioRepository usuarioRepository;
    private final MfaProperties mfaProperties;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier codeVerifier;

    public MfaService(UsuarioRepository usuarioRepository, MfaProperties mfaProperties) {
        this.usuarioRepository = usuarioRepository;
        this.mfaProperties = mfaProperties;
        this.codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);
    }

    public boolean isGerenteWithMfa(Usuario usuario) {
        if (usuario == null || !usuario.isMfaEnabled()) {
            return false;
        }
        return usuario.getRoles().stream()
                .anyMatch(r -> SecurityRoles.GERENTE.equals(r.getNombre()));
    }

    @Transactional
    public MfaSetupResponse setupMfa(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        ensureGerente(usuario);

        String secret = secretGenerator.generate();
        usuario.setTotpSecret(secret);
        usuario.setMfaEnabled(false);
        usuarioRepository.save(usuario);

        String qrUri = buildQrUri(username, secret);
        return new MfaSetupResponse(secret, qrUri);
    }

    @Transactional
    public void confirmMfa(String username, String code) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        ensureGerente(usuario);

        if (usuario.getTotpSecret() == null || !codeVerifier.isValidCode(usuario.getTotpSecret(), code)) {
            throw new IllegalArgumentException("Código TOTP inválido");
        }
        usuario.setMfaEnabled(true);
        usuario.setMfaEnrolledAt(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    public boolean verifyCode(Usuario usuario, String code) {
        if (usuario.getTotpSecret() == null) {
            return false;
        }
        return codeVerifier.isValidCode(usuario.getTotpSecret(), code);
    }

    private void ensureGerente(Usuario usuario) {
        boolean isGerente = usuario.getRoles().stream()
                .anyMatch(r -> SecurityRoles.GERENTE.equals(r.getNombre()));
        if (!isGerente) {
            throw new IllegalArgumentException("MFA solo disponible para cuentas GERENTE");
        }
    }

    private String buildQrUri(String username, String secret) {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(mfaProperties.getIssuer())
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        QrGenerator generator = new ZxingPngQrGenerator();
        try {
            byte[] imageData = generator.generate(data);
            String mimeType = generator.getImageMimeType();
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageData);
        } catch (QrGenerationException e) {
            return String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                    mfaProperties.getIssuer(), username, secret, mfaProperties.getIssuer());
        }
    }
}
