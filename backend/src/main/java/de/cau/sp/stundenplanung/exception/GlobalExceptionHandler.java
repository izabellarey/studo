package de.cau.sp.stundenplanung.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // pfadparam falscher typ
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch: Parameter '{}' hatte Wert '{}', erwartet: {}",
                ex.getName(), ex.getValue(), ex.getRequiredType());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Ungültiger Parameter: " + ex.getName(), request);
    }

    // fehlender benötigter param
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Fehlender Parameter: {}", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Fehlender Parameter: " + ex.getParameterName(), request);
    }

    // requesbody nicht parsebar
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Request-Body nicht lesbar: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Ungültiges Request-Format", request);
    }

    // @Valid auf @RequestBody-DTO schlägt fehl
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validierungsfehler: {}", details);
        return buildResponse(HttpStatus.BAD_REQUEST, details, request);
    }

    // alles andere, infos im log von backend
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unerwarteter Fehler bei {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ein interner Fehler ist aufgetreten", request);
    }


    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(
            NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("Unbekannter Pfad aufgerufen: {}", request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND,
                "Endpunkt nicht gefunden: " + request.getRequestURI(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        log.error("RuntimeException bei {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Ein interner Fehler ist aufgetreten";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }
}