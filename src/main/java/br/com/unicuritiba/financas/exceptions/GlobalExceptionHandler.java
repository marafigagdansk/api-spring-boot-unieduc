package br.com.unicuritiba.financas.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.unicuritiba.financas.dto.ApiErrorResponse;

/**
 * Centraliza o tratamento de exceções da API retornando mensagens claras em JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiErrorResponse.of(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(ValidationException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.of(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.of("Corpo da requisição não contém JSON válido.", HttpStatus.BAD_REQUEST.value()));
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.of("Parâmetro inválido na requisição.", HttpStatus.BAD_REQUEST.value()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiErrorResponse.of("Erro interno ao processar a operação no banco de dados.",
						HttpStatus.INTERNAL_SERVER_ERROR.value()));
	}
}
