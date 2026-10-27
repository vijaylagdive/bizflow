package com.bizflow.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String raw = firstNonBlank(environment.getProperty("DATABASE_URL"), environment.getProperty("DB_URL"),
				environment.getProperty("spring.datasource.url"));
		if (raw == null || raw.startsWith("jdbc:")) {
			return;
		}
		if (!raw.startsWith("postgres://") && !raw.startsWith("postgresql://")) {
			return;
		}

		URI uri = URI.create(raw);
		String userInfo = uri.getUserInfo();
		String username = null;
		String password = null;
		if (userInfo != null) {
			int colon = userInfo.indexOf(':');
			if (colon >= 0) {
				username = decode(userInfo.substring(0, colon));
				password = decode(userInfo.substring(colon + 1));
			}
			else {
				username = decode(userInfo);
			}
		}

		int port = uri.getPort() > 0 ? uri.getPort() : 5432;
		String database = uri.getPath() == null ? "" : uri.getPath();
		if (database.startsWith("/")) {
			database = database.substring(1);
		}

		StringBuilder jdbc = new StringBuilder("jdbc:postgresql://")
				.append(uri.getHost())
				.append(':')
				.append(port)
				.append('/')
				.append(database);
		String query = uri.getQuery();
		if (query == null || query.isBlank()) {
			jdbc.append("?sslmode=require");
		}
		else {
			jdbc.append('?').append(query);
			if (!query.contains("sslmode=")) {
				jdbc.append("&sslmode=require");
			}
		}

		Map<String, Object> props = new HashMap<>();
		props.put("spring.datasource.url", jdbc.toString());
		if (username != null && !username.isBlank()) {
			props.put("spring.datasource.username", username);
		}
		if (password != null) {
			props.put("spring.datasource.password", password);
		}
		environment.getPropertySources().addFirst(new MapPropertySource("convertedDatabaseUrl", props));
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value.trim();
			}
		}
		return null;
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

}
