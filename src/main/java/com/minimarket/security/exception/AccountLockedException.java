package com.minimarket.security.exception;

public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("Cuenta temporalmente bloqueada por intentos fallidos");
    }
}
