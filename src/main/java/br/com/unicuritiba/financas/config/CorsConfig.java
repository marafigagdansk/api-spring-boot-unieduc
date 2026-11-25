package br.com.unicuritiba.financas.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configura CORS para permitir comunicação entre o front-end Web (Vue/Vite), app Android e a API.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final String allowedOrigins;

	public CorsConfig(@Value("${app.cors.allowed-origins:*}") String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] patterns = Arrays.stream(allowedOrigins.split(","))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);

		if (patterns.length == 0) {
			patterns = new String[] { "*" };
		}

		registry.addMapping("/api/**")
				.allowedOriginPatterns(patterns)
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
				.allowedHeaders("*");
	}
}
