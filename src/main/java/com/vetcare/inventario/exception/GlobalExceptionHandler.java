package com.vetcare.inventario.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.vetcare.inventario.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex,
                                                 HttpServletRequest request) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler({RecursoDuplicadoException.class, RecursoEnUsoException.class})
    public ResponseEntity<ApiError> conflicto(RuntimeException ex, HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ApiError> stockInsuficiente(StockInsuficienteException ex,
                                                      HttpServletRequest request) {
        return responder(HttpStatus.CONFLICT, ex.getMessage(), request,
                Map.of("stockActual", ex.getStockDisponible()));
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ApiError> solicitudInvalida(SolicitudInvalidaException ex,
                                                      HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiError> credenciales(CredencialesInvalidasException ex,
                                                 HttpServletRequest request) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacion(MethodArgumentNotValidException ex,
                                               HttpServletRequest request) {
        Map<String, Object> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return responder(HttpStatus.BAD_REQUEST, "Hay datos inválidos en la solicitud", request, campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> cuerpoIlegible(HttpMessageNotReadableException ex,
                                                   HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud falta, no es un JSON válido o tiene valores incorrectos",
                request, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> tipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                   HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido", request, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridad(DataIntegrityViolationException ex,
                                               HttpServletRequest request) {
        log.warn("Violación de integridad en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de los datos", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> inesperado(Exception ex, HttpServletRequest request) {
        // Las excepciones estándar de Spring MVC (404 de ruta, 405, 415...) ya traen su código
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
            return responder(status, status.getReasonPhrase(), request, Map.of());
        }
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno. Intenta nuevamente", request, Map.of());
    }

    private ResponseEntity<ApiError> responder(HttpStatus status, String mensaje,
                                               HttpServletRequest request, Map<String, Object> detalles) {
        ApiError cuerpo = new ApiError(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI(), detalles);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
