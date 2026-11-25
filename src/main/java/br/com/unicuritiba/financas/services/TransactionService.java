package br.com.unicuritiba.financas.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.unicuritiba.financas.dto.TransactionRequest;
import br.com.unicuritiba.financas.dto.TransactionResponse;
import br.com.unicuritiba.financas.exceptions.ResourceNotFoundException;
import br.com.unicuritiba.financas.exceptions.ValidationException;
import br.com.unicuritiba.financas.models.Transaction;
import br.com.unicuritiba.financas.repositories.TransactionRepository;

/**
 * Regras de negócio, validação e operações transacionais sobre lançamentos financeiros.
 */
@Service
public class TransactionService {

	private static final Set<String> VALID_TYPES = Set.of("income", "expense");
	private static final int MAX_DESCRIPTION_LENGTH = 120;
	private static final int MAX_CATEGORY_LENGTH = 60;

	private final TransactionRepository repository;

	public TransactionService(TransactionRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<TransactionResponse> findAll(String type, String search) {
		String normalizedType = normalizeFilterType(type);
		String normalizedSearch = normalizeOptionalText(search);

		List<Transaction> results = (normalizedType == null && normalizedSearch == null)
				? repository.findAllByOrderByDateDescCreatedAtDescIdDesc()
				: repository.searchTransactions(normalizedType, normalizedSearch);

		return results.stream().map(TransactionResponse::fromEntity).toList();
	}

	@Transactional(readOnly = true)
	public TransactionResponse findById(Long id) {
		Transaction tx = findEntityOrThrow(id);
		return TransactionResponse.fromEntity(tx);
	}

	@Transactional
	public TransactionResponse create(TransactionRequest request) {
		Transaction entity = buildValidatedEntity(request, null, null);
		Transaction saved = repository.save(entity);
		return TransactionResponse.fromEntity(saved);
	}

	@Transactional
	public TransactionResponse update(Long id, TransactionRequest request) {
		Transaction existing = findEntityOrThrow(id);
		Transaction validated = buildValidatedEntity(request, existing.getCreatedAt(), Instant.now());

		existing.setType(validated.getType());
		existing.setAmount(validated.getAmount());
		existing.setDescription(validated.getDescription());
		existing.setCategory(validated.getCategory());
		existing.setDate(validated.getDate());
		existing.setUpdatedAt(Instant.now());

		Transaction saved = repository.save(existing);
		return TransactionResponse.fromEntity(saved);
	}

	@Transactional
	public void delete(Long id) {
		Transaction existing = findEntityOrThrow(id);
		repository.delete(existing);
	}

	/**
	 * Valida todos os itens do JSON importado antes de substituir os dados no banco SQL.
	 * Aceita tanto um array direto [...] quanto um objeto { "transactions": [...] }.
	 */
	@Transactional
	public List<TransactionResponse> importTransactions(Object rawPayload) {
		List<?> rawList = extractListFromImportPayload(rawPayload);
		Instant now = Instant.now();
		List<Transaction> entities = new ArrayList<>(rawList.size());

		for (int i = 0; i < rawList.size(); i++) {
			Object item = rawList.get(i);
			if (!(item instanceof Map<?, ?> map)) {
				throw new ValidationException("Registro #" + (i + 1) + " inválido: objeto JSON esperado.");
			}
			try {
				TransactionRequest request = mapToRequest(map);
				Instant createdAt = parseOptionalInstant(map.get("createdAt"), now);
				Instant updatedAt = parseOptionalInstant(map.get("updatedAt"), createdAt);
				entities.add(buildValidatedEntity(request, createdAt, updatedAt));
			} catch (ValidationException ex) {
				throw new ValidationException("Registro #" + (i + 1) + " inválido: " + ex.getMessage());
			}
		}

		repository.deleteAllInBatch();
		repository.saveAll(entities);
		return repository.findAllByOrderByDateDescCreatedAtDescIdDesc()
				.stream()
				.map(TransactionResponse::fromEntity)
				.toList();
	}

	@Transactional
	public List<TransactionResponse> resetToSeed() {
		repository.deleteAllInBatch();
		repository.saveAll(buildSeedEntities());
		return repository.findAllByOrderByDateDescCreatedAtDescIdDesc()
				.stream()
				.map(TransactionResponse::fromEntity)
				.toList();
	}

	@Transactional
	public void seedIfEmpty() {
		if (repository.count() == 0) {
			repository.saveAll(buildSeedEntities());
		}
	}

	private Transaction findEntityOrThrow(Long id) {
		return repository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Lançamento não encontrado."));
	}

	private Transaction buildValidatedEntity(TransactionRequest request, Instant createdAt, Instant updatedAt) {
		if (request == null) {
			throw new ValidationException("Dados da transação inválidos.");
		}

		String type = request.type() != null ? request.type().trim().toLowerCase() : "";
		if (!VALID_TYPES.contains(type)) {
			throw new ValidationException(
					"O tipo da transação deve ser \"income\" (Entrada) ou \"expense\" (Gasto).");
		}

		BigDecimal amount = request.amount();
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ValidationException("Por favor, insira um valor válido maior que zero.");
		}
		BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);

		String description = request.description() != null ? request.description().trim() : "";
		if (description.isEmpty()) {
			throw new ValidationException("A descrição do lançamento é obrigatória.");
		}
		if (description.length() > MAX_DESCRIPTION_LENGTH) {
			throw new ValidationException("A descrição deve ter no máximo 120 caracteres.");
		}

		LocalDate parsedDate = parseRequiredIsoDate(request.date());

		String rawCategory = request.category() != null ? request.category().trim() : "";
		String defaultCategory = "income".equals(type) ? "Salário" : "Outros";
		String category = rawCategory.isEmpty() ? defaultCategory : rawCategory;
		if (category.length() > MAX_CATEGORY_LENGTH) {
			category = category.substring(0, MAX_CATEGORY_LENGTH);
		}

		Instant now = Instant.now();
		Instant effectiveCreatedAt = createdAt != null ? createdAt : now;
		Instant effectiveUpdatedAt = updatedAt != null ? updatedAt : effectiveCreatedAt;

		return new Transaction(
				type,
				normalizedAmount,
				description,
				category,
				parsedDate,
				effectiveCreatedAt,
				effectiveUpdatedAt);
	}

	private LocalDate parseRequiredIsoDate(String rawDate) {
		if (rawDate == null || rawDate.trim().isEmpty()) {
			throw new ValidationException("Informe uma data válida no formato AAAA-MM-DD.");
		}
		try {
			return LocalDate.parse(rawDate.trim());
		} catch (DateTimeParseException ex) {
			throw new ValidationException("Informe uma data válida no formato AAAA-MM-DD.");
		}
	}

	private List<?> extractListFromImportPayload(Object rawPayload) {
		if (rawPayload instanceof List<?> list) {
			return list;
		}
		if (rawPayload instanceof Map<?, ?> map && map.get("transactions") instanceof List<?> list) {
			return list;
		}
		throw new ValidationException(
				"O arquivo JSON deve conter uma lista de transações ou um objeto com a propriedade \"transactions\".");
	}

	private TransactionRequest mapToRequest(Map<?, ?> map) {
		String type = map.get("type") != null ? String.valueOf(map.get("type")) : null;
		BigDecimal amount = parseAmountObject(map.get("amount"));
		String description = map.get("description") != null ? String.valueOf(map.get("description")) : null;
		String category = map.get("category") != null ? String.valueOf(map.get("category")) : null;
		String date = map.get("date") != null ? String.valueOf(map.get("date")) : null;
		return new TransactionRequest(type, amount, description, category, date);
	}

	private BigDecimal parseAmountObject(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		if (value instanceof Number number) {
			return BigDecimal.valueOf(number.doubleValue());
		}
		try {
			return new BigDecimal(String.valueOf(value).trim());
		} catch (NumberFormatException ex) {
			throw new ValidationException("Por favor, insira um valor válido maior que zero.");
		}
	}

	private Instant parseOptionalInstant(Object raw, Instant fallback) {
		if (raw == null) {
			return fallback;
		}
		String str = String.valueOf(raw).trim();
		if (str.isEmpty()) {
			return fallback;
		}
		try {
			return Instant.parse(str);
		} catch (DateTimeParseException ex) {
			return fallback;
		}
	}

	private String normalizeFilterType(String type) {
		if (type == null || type.isBlank() || "all".equalsIgnoreCase(type.trim())) {
			return null;
		}
		String normalized = type.trim().toLowerCase();
		if (!VALID_TYPES.contains(normalized)) {
			throw new ValidationException("Filtro de tipo inválido. Utilize \"income\", \"expense\" ou \"all\".");
		}
		return normalized;
	}

	private String normalizeOptionalText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private List<Transaction> buildSeedEntities() {
		LocalDate today = LocalDate.now();
		Instant now = Instant.now();

		return List.of(
				new Transaction("income", new BigDecimal("4800.00"), "Salário Mensal", "Salário", today, now, now),
				new Transaction("expense", new BigDecimal("1250.00"), "Aluguel do Apartamento", "Moradia", today, now,
						now),
				new Transaction("expense", new BigDecimal("480.50"), "Supermercado Mensal", "Alimentação",
						today.minusDays(1), now, now),
				new Transaction("expense", new BigDecimal("120.00"), "Conta de Energia", "Contas", today.minusDays(2),
						now, now),
				new Transaction("income", new BigDecimal("650.00"), "Projeto Freelance", "Serviços", today.minusDays(3),
						now, now),
				new Transaction("expense", new BigDecimal("89.90"), "Assinaturas de Streaming", "Lazer",
						today.minusDays(4), now, now));
	}
}
