package com.minimarket.security.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MfaConfirmRequest {

    @NotBlank
    @Size(min = 6, max = 6)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
