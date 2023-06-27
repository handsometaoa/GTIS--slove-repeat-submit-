package com.tao.exception;

import com.tao.utils.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Gridsum
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handle(BusinessException e) {
        return Result.failure(e.getCode(), e.getErrorMsg());
    }

}
