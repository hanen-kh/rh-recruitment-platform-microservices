package com.example.employeeservice.employee;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeController {

    private final EmployeService employeService;

    public EmployeController(EmployeService employeService) {
        this.employeService = employeService;
    }

    @PostMapping
    public EmployeResponse create(@Valid @RequestBody EmployeCreateRequest request,
                                  Authentication authentication) {
        return employeService.create(request, authentication);
    }

    @GetMapping
    public List<EmployeResponse> findAll() {
        return employeService.findAll();
    }

    @GetMapping("/{id}")
    public EmployeResponse findById(@PathVariable String id) {
        return employeService.findById(id);
    }

    @PutMapping("/{id}")
    public EmployeResponse update(
            @PathVariable String id,
            @Valid @RequestBody EmployeUpdateRequest request
    ) {
        return employeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        employeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
