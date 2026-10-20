package com.bizflow;

import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DatabaseStatusController {

	private final JdbcTemplate jdbcTemplate;

	public DatabaseStatusController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/db")
	public Map<String, Object> status() {
		return jdbcTemplate.queryForMap("""
				select current_database() as database,
				       current_user as username,
				       version() as version
				""");
	}

}
