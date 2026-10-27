package com.bizflow.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void registerLoginMeAndLogout() throws Exception {
		mockMvc.perform(get("/vendors"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"vijay","password":"secret12"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.userId").isNumber())
				.andExpect(jsonPath("$.username").value("vijay"))
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.passwordHash").doesNotExist());

		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"vijay","password":"wrongpass"}
								"""))
				.andExpect(status().isUnauthorized());

		MvcResult login = mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"vijay","password":"secret12"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("vijay"))
				.andReturn();

		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		mockMvc.perform(get("/auth/me").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("vijay"));

		mockMvc.perform(get("/vendors").session(session))
				.andExpect(status().isOk());

		mockMvc.perform(post("/auth/logout").session(session))
				.andExpect(status().isOk());

		mockMvc.perform(get("/auth/me").session(session))
				.andExpect(status().isUnauthorized());
	}

}
