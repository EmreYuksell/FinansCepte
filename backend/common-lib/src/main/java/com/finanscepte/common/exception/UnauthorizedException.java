package com.finanscepte.common.exception;

/**
 * Kimlik doğrulama başarısız olduğunda (ör. hatalı e-posta/şifre) HTTP 401 döndürülür.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
