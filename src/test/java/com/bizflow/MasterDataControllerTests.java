package com.bizflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MasterDataControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createThenReadMasterData() throws Exception {
		mockMvc.perform(post("/vendors")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Acme Supplies","address":"12 Market St","contactNumber":"9990001111"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.vendorId").isNumber())
				.andExpect(jsonPath("$.name").value("Acme Supplies"));

		mockMvc.perform(get("/vendors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		String vendorLocation = mockMvc.perform(post("/vendors")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Temp Vendor","address":"1 Side Rd","contactNumber":"1112223333"}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(put(vendorLocation)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Updated Vendor","address":"2 Side Rd","contactNumber":"4445556666"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Vendor"))
				.andExpect(jsonPath("$.address").value("2 Side Rd"));

		mockMvc.perform(delete(vendorLocation))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(vendorLocation))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/vendors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(post("/units")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"unitName":"pcs"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.unitName").value("pcs"));

		String unitLocation = mockMvc.perform(post("/units")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"unitName":"box"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.unitName").value("box"))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(put(unitLocation)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"unitName":"kg"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unitName").value("kg"));

		mockMvc.perform(get(unitLocation))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.unitName").value("kg"));

		mockMvc.perform(delete(unitLocation))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(unitLocation))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/units"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		String categoryLocation = mockMvc.perform(post("/product-categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoryName":"Stationery"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.categoryName").value("Stationery"))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		String categoryId = categoryLocation.substring(categoryLocation.lastIndexOf('/') + 1);

		String productLocation = mockMvc.perform(post("/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"productName":"A4 Paper","categoryId":%s}
								""".formatted(categoryId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.productName").value("A4 Paper"))
				.andExpect(jsonPath("$.categoryId").value(categoryId))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(put(productLocation)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"productName":"A4 Ream","categoryId":%s}
								""".formatted(categoryId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productName").value("A4 Ream"));

		mockMvc.perform(get("/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].categoryName").value("Stationery"));

		mockMvc.perform(delete(productLocation))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(productLocation))
				.andExpect(status().isNotFound());

		mockMvc.perform(put(categoryLocation)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoryName":"Office Supplies"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.categoryName").value("Office Supplies"));

		String extraCategoryLocation = mockMvc.perform(post("/product-categories")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"categoryName":"Temp Category"}
								"""))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(delete(extraCategoryLocation))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(extraCategoryLocation))
				.andExpect(status().isNotFound());
	}

}
