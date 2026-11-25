package br.com.unicuritiba.financas.dto;

import java.math.BigDecimal;

/**
 * Payload de entrada para cadastro ou atualização de lançamentos financeiros.
 */
public record TransactionRequest(
		String type,
		BigDecimal amount,
		String description,
		String category,
		String date) {
}
