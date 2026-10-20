package com.bizflow.purchaseorder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class PurchaseOrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createThenReadPurchaseOrder() throws Exception {
		String body = """
				{
				  "purchaseDate": "2026-10-17",
				  "invoiceNumber": "INV-1001",
				  "vendorId": 12,
				  "vendorName": "Acme Supplies",
				  "cashDiscount": 50.00,
				  "items": [
				    {
				      "productId": 101,
				      "qty": 10,
				      "unit": "pcs",
				      "rate": 250.05,
				      "gstPercent": 18.00
				    }
				  ]
				}
				""";

		String location = mockMvc.perform(post("/purchase-orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.invoiceNumber").value("INV-1001"))
				.andExpect(jsonPath("$.vendorName").value("Acme Supplies"))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(101))
				.andExpect(jsonPath("$.items[0].totalAmount").value(2500.50))
				.andExpect(jsonPath("$.cashDiscount").value(50.00))
				.andExpect(jsonPath("$.items[0].gstPercent").value(18.00))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(get(location))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("INV-1001"))
				.andExpect(jsonPath("$.cashDiscount").value(50.00))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].gstPercent").value(18.00));

		mockMvc.perform(get("/purchase-orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(get("/vendor-ledgers").param("vendorId", "12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].type").value("purchase"))
				.andExpect(jsonPath("$[0].vendorName").value("Acme Supplies"))
				.andExpect(jsonPath("$[0].debit").value(2500.50))
				.andExpect(jsonPath("$[0].credit").value(0))
				.andExpect(jsonPath("$[0].balance").value(2500.50));

		mockMvc.perform(get("/inventory/product/101"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].currentStock").value(10.000))
				.andExpect(jsonPath("$[0].stockValue").value(2500.50))
				.andExpect(jsonPath("$[0].unit").value("pcs"));

		String updateBody = """
				{
				  "purchaseDate": "2026-10-18",
				  "invoiceNumber": "INV-1001-A",
				  "vendorId": 12,
				  "vendorName": "Acme Supplies",
				  "cashDiscount": 5.00,
				  "items": [
				    {
				      "productId": 202,
				      "qty": 2,
				      "unit": "kg",
				      "rate": 40.00,
				      "gstPercent": 5.00
				    }
				  ]
				}
				""";

		mockMvc.perform(put(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("INV-1001-A"))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(202))
				.andExpect(jsonPath("$.items[0].gstPercent").value(5.00))
				.andExpect(jsonPath("$.cashDiscount").value(5.00))
				.andExpect(jsonPath("$.purchaseTotalAmount").value(80.00));

		mockMvc.perform(get("/vendor-ledgers").param("vendorId", "12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].debit").value(80.00))
				.andExpect(jsonPath("$[0].balance").value(80.00));

		mockMvc.perform(get("/inventory/product/101"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStock").value(0));

		mockMvc.perform(get("/inventory/product/202"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStock").value(2.000))
				.andExpect(jsonPath("$[0].stockValue").value(80.00));

		mockMvc.perform(delete(location))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(location))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/vendor-ledgers").param("vendorId", "12"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/inventory/product/202"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStock").value(0));
	}

}
