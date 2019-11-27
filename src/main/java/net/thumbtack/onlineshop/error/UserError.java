package net.thumbtack.onlineshop.error;

import lombok.Data;

@Data
public class UserError {
    private UserErrorCode errorCode;
    private String message;
    private String field;

    public UserError(UserErrorCode errorCode, String message, String field) {
        this.errorCode = errorCode;
        this.message = message;
        this.field = field;
    }
}
