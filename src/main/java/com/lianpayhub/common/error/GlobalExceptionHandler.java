package com.lianpayhub.common.error;

import com.lianpayhub.common.api.ApiResponse;
import javax.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.errorCode().code(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validException = (MethodArgumentNotValidException) ex;
            FieldError fieldError = validException.getBindingResult().getFieldError();
            if (fieldError != null) {
                return ApiResponse.error(ErrorCode.BAD_REQUEST.code(),
                        fieldError.getField() + ": " + fieldError.getDefaultMessage());
            }
        }
        return ApiResponse.error(ErrorCode.BAD_REQUEST.code(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpectedException(Exception ex) {
        return ApiResponse.error(ErrorCode.SERVER_ERROR.code(), ErrorCode.SERVER_ERROR.message());
    }
}
