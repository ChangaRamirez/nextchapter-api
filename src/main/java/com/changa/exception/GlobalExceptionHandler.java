package com.changa.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .findFirst()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .orElse("Validation failed");
        ErrorResponseDto errorDto = new ErrorResponseDto(errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidJson(
            HttpMessageNotReadableException ex) {

        ErrorResponseDto errorDto =
                new ErrorResponseDto("Invalid request body. Check that all fields have valid values.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorDto);
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleBookNotFoundException(BookNotFoundException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponseDto);
    }

    @ExceptionHandler(DuplicateBookException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateBookException(DuplicateBookException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponseDto);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                "Invalid value '%s' for parameter '%s'.".formatted(
                        ex.getValue(),
                        ex.getName())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponseDto);
    }

    @ExceptionHandler(InvalidReadingEntryException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidReadingEntry(InvalidReadingEntryException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponseDto);
    }

    @ExceptionHandler(DuplicateReadingEntryException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateReadingEntryException(DuplicateReadingEntryException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponseDto);
    }

    @ExceptionHandler(ReadingEntryNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleReadingEntryNotFoundException(ReadingEntryNotFoundException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponseDto);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateEmailException(DuplicateEmailException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponseDto);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponseDto);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCredentialsException(InvalidCredentialsException ex) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponseDto);
    }
}
