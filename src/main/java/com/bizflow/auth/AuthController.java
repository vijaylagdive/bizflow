package com.bizflow.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bizflow.user.AppUser;
import com.bizflow.user.AppUserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;

	private final SecurityContextRepository securityContextRepository;

	private final AppUserRepository appUserRepository;

	private final PasswordEncoder passwordEncoder;

	public AuthController(AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository, AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthUserResponse> register(@Valid @RequestBody LoginRequest request) {
		String username = request.getUsername().trim();
		if (appUserRepository.existsByUsernameIgnoreCase(username)) {
			throw new ResponseStatusException(CONFLICT, "Username already exists");
		}
		AppUser user = new AppUser();
		user.setUsername(username);
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		AppUser saved = appUserRepository.save(user);
		return ResponseEntity.status(201).body(toResponse(saved));
	}

	@PostMapping("/login")
	public AuthUserResponse login(@Valid @RequestBody LoginRequest body, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			Authentication authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(body.getUsername().trim(), body.getPassword()));
			SecurityContext context = SecurityContextHolder.createEmptyContext();
			context.setAuthentication(authentication);
			SecurityContextHolder.setContext(context);
			securityContextRepository.saveContext(context, request, response);
			request.getSession(true);
			return toResponse(body.getUsername().trim());
		}
		catch (BadCredentialsException ex) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid username or password");
		}
	}

	@GetMapping("/me")
	public AuthUserResponse me(@AuthenticationPrincipal UserDetails principal) {
		if (principal == null) {
			throw new ResponseStatusException(UNAUTHORIZED, "Unauthorized");
		}
		return toResponse(principal.getUsername());
	}

	private AuthUserResponse toResponse(String username) {
		AppUser user = appUserRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Unauthorized"));
		return toResponse(user);
	}

	private static AuthUserResponse toResponse(AppUser user) {
		return new AuthUserResponse(user.getUserId(), user.getUsername());
	}

}
