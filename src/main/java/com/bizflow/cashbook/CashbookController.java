package com.bizflow.cashbook;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bizflow.common.PageResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cashbooks")
public class CashbookController {

	private final CashbookService cashbookService;

	public CashbookController(CashbookService cashbookService) {
		this.cashbookService = cashbookService;
	}

	@GetMapping
	public PageResponse<CashbookEntry> list(@RequestParam(required = false) Long accountId,
			@RequestParam(required = false) String transactionType, @RequestParam(required = false) String fromTo,
			@RequestParam(required = false) LocalDate fromDate, @RequestParam(required = false) LocalDate toDate,
			@PageableDefault(size = 20, sort = { "entryDate", "id" }) Pageable pageable) {
		return cashbookService.list(accountId, transactionType, fromTo, fromDate, toDate, pageable);
	}

	@GetMapping("/{id}")
	public CashbookEntry get(@PathVariable Long id) {
		return cashbookService.get(id);
	}

	@PostMapping
	public ResponseEntity<CashbookEntry> create(@Valid @RequestBody CashbookRequest request) {
		CashbookEntry saved = cashbookService.create(request);
		return ResponseEntity.created(URI.create("/cashbooks/" + saved.getId())).body(saved);
	}

	@PutMapping("/{id}")
	public CashbookEntry update(@PathVariable Long id, @Valid @RequestBody CashbookRequest request) {
		return cashbookService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		cashbookService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
