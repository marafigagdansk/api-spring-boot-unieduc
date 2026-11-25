package br.com.unicuritiba.financas.exceptions;

/**
 * Exceção lançada quando um registro solicitado não existe no banco de dados.
 */
public class ResourceNotFoundException extends RuntimeException {

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
