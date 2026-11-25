package br.com.unicuritiba.financas.exceptions;

/**
 * Exceção lançada quando os dados enviados na requisição violam regras de negócio ou formato.
 */
public class ValidationException extends RuntimeException {

	public ValidationException(String message) {
		super(message);
	}
}
