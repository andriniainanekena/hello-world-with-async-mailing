package api.poja.app.endpoint.rest.controller;

import api.poja.app.endpoint.rest.model.ErrorResponse;
import api.poja.app.service.exception.InvalidEmailException;
import api.poja.app.service.exception.UnsupportedImageTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({
    UnsupportedImageTypeException.class,
    InvalidEmailException.class,
    MissingServletRequestParameterException.class,
    MissingServletRequestPartException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorResponse(exception.getMessage()));
  }
}
