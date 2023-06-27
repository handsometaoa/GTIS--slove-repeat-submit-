package com.tao.exception;

/**
 * @author Gridsum
 */
public class BusinessException extends RuntimeException {
    private final String code;
    private final String errorMsg;

    private BusinessException(String code, String errorMsg) {
        super(errorMsg);
        this.code = code;
        this.errorMsg = errorMsg;
    }

    public static BusinessException createException(String code, String errorMsg) {
        return new BusinessException(code, errorMsg);
    }

    public String getCode() {
        return code;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
