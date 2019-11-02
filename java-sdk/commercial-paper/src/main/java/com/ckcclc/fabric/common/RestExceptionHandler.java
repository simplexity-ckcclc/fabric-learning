package com.ckcclc.fabric.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * AOP handler for request exception
 */
@ControllerAdvice(basePackages = "com.meitu.mlink.backend.controller")
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e, HttpServletRequest servletRequest) {
        ErrorCode errorCode;
        if (e instanceof ServiceException) {
            errorCode = ((ServiceException) e).getErrorCode();
        } else {
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            logger.error("INTERNAL_SERVER_ERROR caught!", e);
        }

        Result result = Result.fail(errorCode).withErrorMsg(e.getMessage());
        logger.warn("[Response] return:{} for request uri:{} from ip:{}, exception cause:{}",
                result, servletRequest.getRequestURI(), servletRequest.getRemoteHost(), e.getMessage());
        return new ResponseEntity<>(result, errorCode == ErrorCode.INTERNAL_SERVER_ERROR ? HttpStatus.INTERNAL_SERVER_ERROR : HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {

        Result result = Result.fail(ErrorCode.REQUEST_PARAMETER_ERROR).withErrorMsg(ex.getMessage());
        logger.info("[Response] return:{} for request description:{}",
                result, request.getDescription(true));
        return super.handleExceptionInternal(ex, ex.getMessage(), headers, status, request);
    }
}