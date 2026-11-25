package br.com.unicuritiba.financas.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.unicuritiba.financas.dto.DeleteResponse;
import br.com.unicuritiba.financas.dto.TransactionRequest;
import br.com.unicuritiba.financas.dto.TransactionResponse;
import br.com.unicuritiba.financas.services.TransactionService;

/**
 * Controller REST responsável pelo CRUD completo e utilitários de lançamentos financeiros.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService service;

	public TransactionController(TransactionService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<TransactionResponse>> getAllTransactions(
			@RequestParam(required = false) String type,
			@RequestParam(required = false) String search) {
		return ResponseEntity.ok(service.findAll(type, search));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long id) {
		return ResponseEntity.ok(service.findById(id));
	}

	@PostMapping
	public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
		TransactionResponse created = service.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransactionResponse> updateTransaction(
			@PathVariable Long id,
			@RequestBody TransactionRequest request) {
		return ResponseEntity.ok(service.update(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<DeleteResponse> deleteTransaction(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.ok(new DeleteResponse(id));
	}

	@PostMapping("/import")
	public ResponseEntity<List<TransactionResponse>> importTransactions(@RequestBody Object rawPayload) {
		return ResponseEntity.ok(service.importTransactions(rawPayload));
	}

	@PostMapping("/reset")
	public ResponseEntity<List<TransactionResponse>> resetTransactions() {
		return ResponseEntity.ok(service.resetToSeed());
	}
}
