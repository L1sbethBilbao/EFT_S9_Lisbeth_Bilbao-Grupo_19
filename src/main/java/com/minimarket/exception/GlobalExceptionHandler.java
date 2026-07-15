package com.minimarket.exception;

import com.minimarket.security.monitor.SuspiciousActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private SuspiciousActivityService suspiciousActivityService;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (first, second) -> first));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Datos de entrada inválidos",
                "detalles", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex, WebRequest request) {
        recordFailedLoginIfPossible(request, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales inválidas"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        recordFailedLoginIfPossible(request, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Credenciales inválidas"));
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Map<String, String>> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Cuenta temporalmente bloqueada por intentos fallidos"));
    }

    @ExceptionHandler(com.minimarket.security.exception.AccountLockedException.class)
    public ResponseEntity<Map<String, String>> handleAccountLocked(
            com.minimarket.security.exception.AccountLockedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(com.minimarket.security.exception.InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRefreshToken(
            com.minimarket.security.exception.InvalidRefreshTokenException ex, WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            suspiciousActivityService.recordInvalidJwt(servletWebRequest.getRequest(), ex);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(com.minimarket.exception.StockInsuficienteException.class)
    public ResponseEntity<Map<String, String>> handleStockInsuficiente(
            com.minimarket.exception.StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(com.minimarket.exception.UsuarioIncompletoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioIncompleto(
            com.minimarket.exception.UsuarioIncompletoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "No se puede eliminar: existen registros relacionados"));
    }

    private void recordFailedLoginIfPossible(WebRequest request, String username) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest httpReq = servletWebRequest.getRequest();
            suspiciousActivityService.recordFailedLogin(httpReq, username);
        }
    }
}
