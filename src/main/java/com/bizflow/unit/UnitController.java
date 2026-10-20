package com.bizflow.unit;

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

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/units")
public class UnitController {

	private final UnitRepository unitRepository;

	public UnitController(UnitRepository unitRepository) {
		this.unitRepository = unitRepository;
	}

	@GetMapping
	public List<Unit> list() {
		return unitRepository.findAll();
	}

	@GetMapping("/{unitId}")
	public Unit get(@PathVariable Long unitId) {
		return unitRepository.findById(unitId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unit not found"));
	}

	@PostMapping
	public ResponseEntity<Unit> create(@Valid @RequestBody UnitRequest request) {
		Unit unit = new Unit();
		unit.setUnitName(request.getUnitName());
		Unit saved = unitRepository.save(unit);
		return ResponseEntity.created(URI.create("/units/" + saved.getUnitId())).body(saved);
	}

	@PutMapping("/{unitId}")
	public Unit update(@PathVariable Long unitId, @Valid @RequestBody UnitRequest request) {
		Unit unit = unitRepository.findById(unitId)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unit not found"));
		unit.setUnitName(request.getUnitName());
		return unitRepository.save(unit);
	}

	@DeleteMapping("/{unitId}")
	public ResponseEntity<Void> delete(@PathVariable Long unitId) {
		if (!unitRepository.existsById(unitId)) {
			throw new ResponseStatusException(NOT_FOUND, "Unit not found");
		}
		unitRepository.deleteById(unitId);
		return ResponseEntity.noContent().build();
	}

}
