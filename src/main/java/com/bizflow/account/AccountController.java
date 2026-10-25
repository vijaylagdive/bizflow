package com.bizflow.account;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/accounts")
public class AccountController {

	private final AccountRepository accountRepository;

	public AccountController(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@GetMapping
	public List<Account> list() {
		return accountRepository.findAll();
	}

	@GetMapping("/{accountId}")
	public Account get(@PathVariable Long accountId) {
		return accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
	}

	@PostMapping
	public ResponseEntity<Account> create(@Valid @RequestBody AccountRequest request) {
		Account account = new Account();
		copyNameAndType(account, request);
		Account saved = accountRepository.save(account);
		return ResponseEntity.created(URI.create("/accounts/" + saved.getAccountId())).body(saved);
	}

	@PutMapping("/{accountId}")
	public Account update(@PathVariable Long accountId, @Valid @RequestBody AccountRequest request) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found"));
		copyNameAndType(account, request);
		return accountRepository.save(account);
	}

	@DeleteMapping("/{accountId}")
	public ResponseEntity<Void> delete(@PathVariable Long accountId) {
		if (!accountRepository.existsById(accountId)) {
			throw new ResponseStatusException(NOT_FOUND, "Account not found");
		}
		accountRepository.deleteById(accountId);
		return ResponseEntity.noContent().build();
	}

	private static void copyNameAndType(Account account, AccountRequest request) {
		String type = normalizeAccountType(request.getAccountType());
		if (type == null) {
			throw new ResponseStatusException(BAD_REQUEST, "accountType must be Saving or Current");
		}
		account.setName(request.getAccountName().trim());
		account.setAccountType(type);
	}

	private static String normalizeAccountType(String accountType) {
		if (accountType == null) {
			return null;
		}
		String type = accountType.trim().toLowerCase();
		if ("saving".equals(type) || "savings".equals(type)) {
			return "Saving";
		}
		if ("current".equals(type)) {
			return "Current";
		}
		return null;
	}

}
