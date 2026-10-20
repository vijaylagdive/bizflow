package com.bizflow;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public Map<String, String> hello() {
		return Map.of("app", "BizFlow 1", "status", "running application");
	}

}
