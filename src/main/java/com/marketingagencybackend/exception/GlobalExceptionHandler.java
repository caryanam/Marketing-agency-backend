package com.marketingagencybackend.exception;

import com.marketingagencybackend.dto.ApiResponseDTO;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.springframework.http.converter.HttpMessageNotReadableException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //Generic Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleGeneric(
            Exception ex) {

        log.error("Unhandled exception", ex);

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later."
        );
    }

    // Mail Service Exception
    @ExceptionHandler(org.springframework.mail.MailException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMailException(
            org.springframework.mail.MailException ex) {

        log.error("Mail error occurred", ex);

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Mail service error: " + (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage())
        );
    }


    //Authentication Failed
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBadCredentials(
            BadCredentialsException ex) {

        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }


    //Access Denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleAccessDenied(
            AccessDeniedException ex) {

        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }


    //DTO Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(
                HttpStatus.BAD_REQUEST,
                message.isBlank() ? "Invalid request data." : message
        );
    }


    //Path Variable / Request Param Validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleConstraintViolation(
            ConstraintViolationException ex) {

        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }


    //Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    //Duplicate Resource
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleDuplicate(
            DuplicateResourceException ex) {

        return build(HttpStatus.CONFLICT, ex.getMessage());
    }


     //Illegal Argument
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }




    //Common Response Builder
    private ResponseEntity<ApiResponseDTO<Object>> build(
            HttpStatus status,
            String message) {

        ApiResponseDTO<Object> response = new ApiResponseDTO<>(
                "FAIL",
                message,
                null
        );

        return ResponseEntity.status(status).body(response);
    }

    //Url Exception
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleNoResourceFound(
            NoResourceFoundException ex) {

        log.warn("No resource found: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDTO<>(
                        "FAIL",
                        "API endpoint not found.",
                        null
                ));
    }


    //Invalid Enums Exception
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex) {

        String message = "Invalid request body.";

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatException) {

            Class<?> targetType = invalidFormatException.getTargetType();

            if (targetType.isEnum()) {

                String validValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message = "Invalid value '" +
                        invalidFormatException.getValue() +
                        "' for field '" +
                        invalidFormatException.getPath().get(0).getFieldName() +
                        "'. Allowed values are: " +
                        validValues;
            }
        }

        return ResponseEntity.badRequest().body(
                new ApiResponseDTO<>(
                        "FAIL",
                        message,
                        null
                )
        );
    }
}
