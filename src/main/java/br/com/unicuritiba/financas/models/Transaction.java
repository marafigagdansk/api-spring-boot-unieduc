package br.com.unicuritiba.financas.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entidade JPA que representa um lançamento financeiro (entrada ou gasto) no banco SQL.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 16)
	private String type;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false, length = 120)
	private String description;

	@Column(nullable = false, length = 60)
	private String category;

	@Column(name = "transaction_date", nullable = false)
	private LocalDate date;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public Transaction() {
	}

	public Transaction(
			String type,
			BigDecimal amount,
			String description,
			String category,
			LocalDate date,
			Instant createdAt,
			Instant updatedAt) {
		this.type = type;
		this.amount = amount;
		this.description = description;
		this.category = category;
		this.date = date;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		if (this.createdAt == null) {
			this.createdAt = now;
		}
		if (this.updatedAt == null) {
			this.updatedAt = this.createdAt;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
