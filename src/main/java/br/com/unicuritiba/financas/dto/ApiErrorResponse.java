package br.com.unicuritiba.financas.dto;

import java.time.Instant;

/**
 * Formato padrão de resposta de erro da API REST, consumido pelo front-end Web e app Android.
 */
public record ApiErrorResponse(
		String message,
		int status,
		String timestamp) {

	public static ApiErrorResponse of(String message, int status) {
		return new ApiErrorResponse(message, status, Instant.now().toString());
	}
}
