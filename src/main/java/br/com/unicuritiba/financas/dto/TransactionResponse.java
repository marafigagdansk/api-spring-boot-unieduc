package br.com.unicuritiba.financas.dto;

import java.math.BigDecimal;

import br.com.unicuritiba.financas.models.Transaction;

/**
 * Representação JSON padronizada de uma transação retornada pela API REST.
 */
public record TransactionResponse(
		Long id,
		String type,
		BigDecimal amount,
		String description,
		String category,
		String date,
		String createdAt,
		String updatedAt) {

	public static TransactionResponse fromEntity(Transaction tx) {
		return new TransactionResponse(
				tx.getId(),
				tx.getType(),
				tx.getAmount(),
				tx.getDescription(),
				tx.getCategory(),
				tx.getDate() != null ? tx.getDate().toString() : null,
				tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null,
				tx.getUpdatedAt() != null ? tx.getUpdatedAt().toString() : null);
	}
}
