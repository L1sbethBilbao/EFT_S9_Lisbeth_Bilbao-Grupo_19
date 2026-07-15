package com.minimarket.exception;

import com.minimarket.security.exception.AccountLockedException;
import com.minimarket.security.exception.InvalidRefreshTokenException;
import com.minimarket.security.monitor.SuspiciousActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private SuspiciousActivityService suspiciousActivityService;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Validation THEN Retorna400 Con Detalles")
    void givenDefaultContext_whenHandleValidation_thenRetorna400ConDetalles() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError emailError = new FieldError("dto", "email", "email inválido");
        FieldError emailDuplicado = new FieldError("dto", "email", "email requerido");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(emailError, emailDuplicado));

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Datos de entrada inválidos");
        @SuppressWarnings("unchecked")
        Map<String, String> detalles = (Map<String, String>) response.getBody().get("detalles");
        assertThat(detalles).containsEntry("email", "email inválido");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Illegal Argument THEN Retorna409")
    void givenDefaultContext_whenHandleIllegalArgument_thenRetorna409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("conflicto"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "conflicto");
    }

    @Test
    @DisplayName("GIVEN Con Servlet Web Request WHEN Handle Authentication THEN Registra Intento Fallido")
    void givenConServletWebRequest_whenHandleAuthentication_thenRegistraIntentoFallido() {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(httpRequest);
        AuthenticationException ex = new AuthenticationException("fallo") {
        };

        ResponseEntity<Map<String, String>> response = handler.handleAuthentication(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Credenciales inválidas");
        verify(suspiciousActivityService).recordFailedLogin(httpRequest, null);
    }

    @Test
    @DisplayName("GIVEN Sin Servlet Web Request WHEN Handle Authentication THEN No Registra Intento")
    void givenSinServletWebRequest_whenHandleAuthentication_thenNoRegistraIntento() {
        WebRequest webRequest = mock(WebRequest.class);

        ResponseEntity<Map<String, String>> response = handler.handleAuthentication(
                new AuthenticationException("fallo") {
                }, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(suspiciousActivityService);
    }

    @Test
    @DisplayName("GIVEN Con Servlet Web Request WHEN Handle Bad Credentials THEN Registra Intento Fallido")
    void givenConServletWebRequest_whenHandleBadCredentials_thenRegistraIntentoFallido() {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(httpRequest);

        ResponseEntity<Map<String, String>> response =
                handler.handleBadCredentials(new BadCredentialsException("bad"), webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(suspiciousActivityService).recordFailedLogin(httpRequest, null);
    }

    @Test
    @DisplayName("GIVEN Sin Servlet Web Request WHEN Handle Bad Credentials THEN No Registra Intento")
    void givenSinServletWebRequest_whenHandleBadCredentials_thenNoRegistraIntento() {
        ResponseEntity<Map<String, String>> response = handler.handleBadCredentials(
                new BadCredentialsException("bad"), mock(WebRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(suspiciousActivityService);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Locked THEN Retorna429")
    void givenDefaultContext_whenHandleLocked_thenRetorna429() {
        ResponseEntity<Map<String, String>> response =
                handler.handleLocked(new LockedException("bloqueada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).containsEntry("error",
                "Cuenta temporalmente bloqueada por intentos fallidos");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Account Locked THEN Retorna429 Con Mensaje")
    void givenDefaultContext_whenHandleAccountLocked_thenRetorna429ConMensaje() {
        ResponseEntity<Map<String, String>> response = handler.handleAccountLocked(new AccountLockedException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).containsEntry("error",
                "Cuenta temporalmente bloqueada por intentos fallidos");
    }

    @Test
    @DisplayName("GIVEN Con Servlet Web Request WHEN Handle Invalid Refresh Token THEN Registra Jwt Invalido")
    void givenConServletWebRequest_whenHandleInvalidRefreshToken_thenRegistraJwtInvalido() {
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();
        ServletWebRequest webRequest = new ServletWebRequest(httpRequest);
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("token inválido");

        ResponseEntity<Map<String, String>> response = handler.handleInvalidRefreshToken(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "token inválido");
        verify(suspiciousActivityService).recordInvalidJwt(httpRequest, ex);
    }

    @Test
    @DisplayName("GIVEN Sin Servlet Web Request WHEN Handle Invalid Refresh Token THEN No Registra Jwt Invalido")
    void givenSinServletWebRequest_whenHandleInvalidRefreshToken_thenNoRegistraJwtInvalido() {
        InvalidRefreshTokenException ex = new InvalidRefreshTokenException("token inválido");

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidRefreshToken(ex, mock(WebRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(suspiciousActivityService);
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Stock Insuficiente THEN Retorna409")
    void givenDefaultContext_whenHandleStockInsuficiente_thenRetorna409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleStockInsuficiente(new StockInsuficienteException("sin stock"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "sin stock");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Usuario Incompleto THEN Retorna400")
    void givenDefaultContext_whenHandleUsuarioIncompleto_thenRetorna400() {
        ResponseEntity<Map<String, String>> response =
                handler.handleUsuarioIncompleto(new UsuarioIncompletoException("perfil incompleto"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "perfil incompleto");
    }

    @Test
    @DisplayName("GIVEN Default Context WHEN Handle Data Integrity THEN Retorna409")
    void givenDefaultContext_whenHandleDataIntegrity_thenRetorna409() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("fk"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error",
                "No se puede eliminar: existen registros relacionados");
    }
}
