package br.com.unicuritiba.financas;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import br.com.unicuritiba.financas.repositories.TransactionRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerIntegrationTest {

	private static final Pattern ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

	@LocalServerPort
	private int port;

	@Autowired
	private TransactionRepository repository;

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@BeforeEach
	void cleanDatabase() {
		repository.deleteAll();
	}

	private String baseUrl() {
		return "http://localhost:" + port + "/api/transactions";
	}

	private HttpResponse<String> send(String method, String path, String jsonBody) throws Exception {
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.uri(URI.create(baseUrl() + path))
				.header("Content-Type", "application/json");

		if (jsonBody != null) {
			builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
		} else {
			builder.method(method, HttpRequest.BodyPublishers.noBody());
		}

		return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}

	private Long extractId(String json) {
		Matcher matcher = ID_PATTERN.matcher(json);
		assertThat(matcher.find()).as("ID presente no JSON: %s", json).isTrue();
		return Long.parseLong(matcher.group(1));
	}

	@Test
	void shouldExecuteFullCrudLifecycle() throws Exception {
		// 1. POST (Cadastro)
		String createBody = """
				{
				  "type": "expense",
				  "amount": 125.50,
				  "description": "Mercado da Semana",
				  "category": "Alimentação",
				  "date": "2026-11-23"
				}
				""";
		HttpResponse<String> createResponse = send("POST", "", createBody);
		assertThat(createResponse.statusCode()).isEqualTo(201);
		assertThat(createResponse.body()).contains("Mercado da Semana", "125.50", "2026-11-23");
		Long createdId = extractId(createResponse.body());

		// 2. GET (Listagem)
		HttpResponse<String> listResponse = send("GET", "", null);
		assertThat(listResponse.statusCode()).isEqualTo(200);
		assertThat(listResponse.body()).contains("Mercado da Semana");

		// 3. GET /{id} (Detalhe)
		HttpResponse<String> detailResponse = send("GET", "/" + createdId, null);
		assertThat(detailResponse.statusCode()).isEqualTo(200);
		assertThat(detailResponse.body()).contains("Alimentação");

		// 4. PUT /{id} (Edição)
		String updateBody = """
				{
				  "type": "income",
				  "amount": 250.00,
				  "description": "Reembolso Mercado",
				  "category": "Serviços",
				  "date": "2026-11-24"
				}
				""";
		HttpResponse<String> updateResponse = send("PUT", "/" + createdId, updateBody);
		assertThat(updateResponse.statusCode()).isEqualTo(200);
		assertThat(updateResponse.body()).contains("Reembolso Mercado", "income", "250.00");

		// 5. DELETE /{id} (Exclusão)
		HttpResponse<String> deleteResponse = send("DELETE", "/" + createdId, null);
		assertThat(deleteResponse.statusCode()).isEqualTo(200);
		assertThat(deleteResponse.body()).contains("\"deletedId\":" + createdId);

		// 6. GET /{id} após exclusão -> 404
		HttpResponse<String> missingResponse = send("GET", "/" + createdId, null);
		assertThat(missingResponse.statusCode()).isEqualTo(404);
	}

	@Test
	void shouldValidateInvalidPayloadAndSupportImportAndReset() throws Exception {
		// Validação 400
		String invalidBody = """
				{
				  "type": "expense",
				  "amount": -10,
				  "description": "",
				  "date": "23-11-2026"
				}
				""";
		HttpResponse<String> badRequest = send("POST", "", invalidBody);
		assertThat(badRequest.statusCode()).isEqualTo(400);
		assertThat(badRequest.body()).contains("message");

		// Importação via array JSON
		String importBody = """
				[
				  {
				    "type": "income",
				    "amount": 1500,
				    "description": "Freela Backend",
				    "category": "Serviços",
				    "date": "2026-11-20"
				  }
				]
				""";
		HttpResponse<String> importResponse = send("POST", "/import", importBody);
		assertThat(importResponse.statusCode()).isEqualTo(200);
		assertThat(importResponse.body()).contains("Freela Backend");
		assertThat(repository.count()).isEqualTo(1);

		// Reset para dados de exemplo
		HttpResponse<String> resetResponse = send("POST", "/reset", null);
		assertThat(resetResponse.statusCode()).isEqualTo(200);
		assertThat(repository.count()).isEqualTo(6);
	}
}
