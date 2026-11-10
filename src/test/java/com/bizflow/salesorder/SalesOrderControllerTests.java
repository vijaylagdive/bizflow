package com.bizflow.salesorder;

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
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class SalesOrderControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createThenReadSalesOrderAndPostLedgerAndInventory() throws Exception {
		mockMvc.perform(post("/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ravi Stores","address":"Main Road","contactNumber":"9000000001"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerId").isNumber());

		mockMvc.perform(post("/purchase-orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "purchaseDate": "2026-10-20",
								  "invoiceNumber": "INV-STOCK",
								  "vendorId": 1,
								  "vendorName": "Stock Vendor",
								  "items": [
								    {
								      "productId": 303,
								      "qty": 10,
								      "unit": "kg",
								      "rate": 50.00,
								      "gstPercent": 5.00
								    }
								  ]
								}
								"""))
				.andExpect(status().isCreated());

		String saleBody = """
				{
				  "salesDate": "2026-10-25",
				  "invoiceNumber": "SAL-001",
				  "customerId": 1,
				  "customerName": "Ravi Stores",
				  "cashDiscount": 20.00,
				  "cgst": 7.50,
				  "sgst": 7.50,
				  "items": [
				    {
				      "productId": 303,
				      "qty": 4,
				      "unit": "kg",
				      "rate": 75.00,
				      "discount": 20.00,
				      "gstPercent": 5.00
				    }
				  ]
				}
				""";

		String location = mockMvc.perform(post("/sales-orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(saleBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.invoiceNumber").value("SAL-001"))
				.andExpect(jsonPath("$.customerName").value("Ravi Stores"))
				.andExpect(jsonPath("$.cashDiscount").value(20.00))
				.andExpect(jsonPath("$.cgst").value(7.50))
				.andExpect(jsonPath("$.sgst").value(7.50))
				.andExpect(jsonPath("$.items", hasSize(1)))
				.andExpect(jsonPath("$.items[0].productId").value(303))
				.andExpect(jsonPath("$.items[0].discount").value(20.00))
				.andExpect(jsonPath("$.items[0].totalAmount").value(280.00))
				.andExpect(jsonPath("$.salesTotalAmount").value(280.00))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(get(location))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("SAL-001"))
				.andExpect(jsonPath("$.items", hasSize(1)));

		mockMvc.perform(get("/sales-orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.totalElements").value(1));

		mockMvc.perform(get("/customer-ledgers").param("customerId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].customerId").value(1))
				.andExpect(jsonPath("$[0].customerName").value("Ravi Stores"))
				.andExpect(jsonPath("$[0].currentBalance").value(280.00));

		mockMvc.perform(get("/inventory/product/303"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStock").value(6.000))
				.andExpect(jsonPath("$[0].unit").value("kg"));

		String updateBody = """
				{
				  "salesDate": "2026-10-26",
				  "invoiceNumber": "SAL-001-A",
				  "customerId": 1,
				  "customerName": "Ravi Stores",
				  "cashDiscount": 5.00,
				  "cgst": 4.00,
				  "sgst": 4.00,
				  "items": [
				    {
				      "productId": 303,
				      "qty": 2,
				      "unit": "kg",
				      "rate": 80.00,
				      "discount": 10.00,
				      "gstPercent": 5.00
				    }
				  ]
				}
				""";

		mockMvc.perform(put(location)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.invoiceNumber").value("SAL-001-A"))
				.andExpect(jsonPath("$.items[0].qty").value(2.000))
				.andExpect(jsonPath("$.items[0].discount").value(10.00))
				.andExpect(jsonPath("$.cgst").value(4.00))
				.andExpect(jsonPath("$.sgst").value(4.00))
				.andExpect(jsonPath("$.salesTotalAmount").value(150.00));

		mockMvc.perform(get("/customer-ledgers").param("customerId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].currentBalance").value(150.00));

		mockMvc.perform(get("/inventory/product/303"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStock").value(8.000));

		mockMvc.perform(delete(location))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(location))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/customer-ledgers").param("customerId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/inventory/product/303"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].currentStock").value(10.000));
	}

	@Test
	void listsSalesOrdersByDateWithPagination() throws Exception {
		mockMvc.perform(post("/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ravi Stores"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/purchase-orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "purchaseDate": "2026-10-20",
								  "invoiceNumber": "INV-STOCK",
								  "vendorId": 1,
								  "vendorName": "Stock Vendor",
								  "items": [
								    {"productId": 303, "qty": 10, "unit": "kg", "rate": 50.00, "gstPercent": 5.00}
								  ]
								}
								"""))
				.andExpect(status().isCreated());

		postSale("2026-10-25", "SAL-EARLIER");
		postSale("2026-10-27", "SAL-LATER");

		mockMvc.perform(get("/sales-orders").param("fromDate", "2026-10-27").param("toDate", "2026-10-27"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].invoiceNumber").value("SAL-LATER"));

		mockMvc.perform(get("/sales-orders").param("page", "0").param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].invoiceNumber").value("SAL-EARLIER"))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	private void postSale(String salesDate, String invoiceNumber) throws Exception {
		mockMvc.perform(post("/sales-orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "salesDate": "%s",
								  "invoiceNumber": "%s",
								  "customerId": 1,
								  "customerName": "Ravi Stores",
								  "items": [
								    {"productId": 303, "qty": 1, "unit": "kg", "rate": 75.00, "gstPercent": 5.00}
								  ]
								}
								""".formatted(salesDate, invoiceNumber)))
				.andExpect(status().isCreated());
	}

}
