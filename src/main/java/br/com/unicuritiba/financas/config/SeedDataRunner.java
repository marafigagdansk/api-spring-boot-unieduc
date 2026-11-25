package br.com.unicuritiba.financas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.unicuritiba.financas.services.TransactionService;

/**
 * Popula dados iniciais de demonstração apenas quando APP_SEED_ON_EMPTY=true e o banco está vazio.
 */
@Component
public class SeedDataRunner implements ApplicationRunner {

	private final TransactionService transactionService;
	private final boolean seedOnEmpty;

	public SeedDataRunner(
			TransactionService transactionService,
			@Value("${app.seed-on-empty:false}") boolean seedOnEmpty) {
		this.transactionService = transactionService;
		this.seedOnEmpty = seedOnEmpty;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (seedOnEmpty) {
			transactionService.seedIfEmpty();
		}
	}
}
