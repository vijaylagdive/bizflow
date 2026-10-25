package com.bizflow.cashbook;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CashbookControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void postsCashbookBalanceAndLedgers() throws Exception {
		String accountLocation = mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountName":"Cash","accountType":"Saving"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accountName").value("Cash"))
				.andExpect(jsonPath("$.accountType").value("Saving"))
				.andReturn()
				.getResponse()
				.getHeader("Location");
		String accountId = accountLocation.substring(accountLocation.lastIndexOf('/') + 1);

		String bankLocation = mockMvc.perform(post("/accounts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"accountName":"HDFC","type":"Current"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accountName").value("HDFC"))
				.andExpect(jsonPath("$.accountType").value("Current"))
				.andReturn()
				.getResponse()
				.getHeader("Location");
		String bankAccountId = bankLocation.substring(bankLocation.lastIndexOf('/') + 1);

		mockMvc.perform(post("/vendors")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Acme Supplies","address":"12 Market St","contactNumber":"9990001111"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Ravi Stores","address":"Main Road","contactNumber":"9000000001"}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/cashbooks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "date": "2026-10-25",
								  "transactionType": "receipt",
								  "accountId": %s,
								  "fromTo": "other",
								  "amount": 1000.00,
								  "remark": "opening cash"
								}
								""".formatted(accountId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.transactionType").value("receipt"))
				.andExpect(jsonPath("$.account").value("Cash"))
				.andExpect(jsonPath("$.fromTo").value("other"))
				.andExpect(jsonPath("$.partyName").doesNotExist())
				.andExpect(jsonPath("$.balance").value(1000.00));

		mockMvc.perform(get("/customer-ledgers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/vendor-ledgers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(post("/cashbooks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "date": "2026-10-26",
								  "transactionType": "payment",
								  "accountId": %s,
								  "fromTo": "Acme Supplies",
								  "partyId": 1,
								  "amount": 250.00,
								  "remark": "vendor payment"
								}
								""".formatted(bankAccountId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.account").value("HDFC"))
				.andExpect(jsonPath("$.fromTo").value("Acme Supplies"))
				.andExpect(jsonPath("$.balance").value(750.00));

		mockMvc.perform(get("/vendor-ledgers").param("vendorId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].type").value("payment"))
				.andExpect(jsonPath("$[0].credit").value(250.00))
				.andExpect(jsonPath("$[0].debit").value(0))
				.andExpect(jsonPath("$[0].balance").value(-250.00));

		mockMvc.perform(get("/vendors/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentBalance").value(-250.00));

		String receiptLocation = mockMvc.perform(post("/cashbooks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "date": "2026-10-27",
								  "transactionType": "receipt",
								  "accountId": %s,
								  "fromTo": "Ravi Stores",
								  "partyId": 1,
								  "amount": 300.00,
								  "remark": "customer receipt"
								}
								""".formatted(accountId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.fromTo").value("Ravi Stores"))
				.andExpect(jsonPath("$.balance").value(1050.00))
				.andReturn()
				.getResponse()
				.getHeader("Location");

		mockMvc.perform(get("/customer-ledgers").param("customerId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].type").value("receipt"))
				.andExpect(jsonPath("$[0].credit").value(300.00))
				.andExpect(jsonPath("$[0].balance").value(-300.00));

		mockMvc.perform(get("/customers/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.currentBalance").value(-300.00));

		mockMvc.perform(get("/cashbooks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[2].balance").value(1050.00));

		mockMvc.perform(delete(receiptLocation))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/customer-ledgers").param("customerId", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(get("/cashbooks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[1].account").value("HDFC"))
				.andExpect(jsonPath("$[1].balance").value(750.00));
	}

}
