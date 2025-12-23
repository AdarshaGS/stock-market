package com.lending.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lending.data.LendingDTO;
import com.lending.data.RepaymentDTO;
import com.lending.service.LendingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/lending")
@RequiredArgsConstructor
@Tag(name = "Lending Money", description = "APIs for tracking money lent to friends and family")
public class LendingController {

    private final LendingService lendingService;

    @PostMapping
    @Operation(summary = "Add new lending record")
    public ResponseEntity<LendingDTO> addLending(@RequestBody LendingDTO lendingDTO) {
        return ResponseEntity.ok(lendingService.createLending(lendingDTO));
    }

    @GetMapping
    @Operation(summary = "List all lendings for a user")
    public ResponseEntity<List<LendingDTO>> getLendings(@RequestParam Long userId) {
        return ResponseEntity.ok(lendingService.getUserLendings(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lending record details")
    public ResponseEntity<LendingDTO> getLendingById(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.getLendingById(id));
    }

    @PostMapping("/{id}/repayment")
    @Operation(summary = "Add a repayment to a lending record")
    public ResponseEntity<LendingDTO> addRepayment(@PathVariable Long id, @RequestBody RepaymentDTO repaymentDTO) {
        return ResponseEntity.ok(lendingService.addRepayment(id, repaymentDTO));
    }

    @PutMapping("/{id}/close")
    @Operation(summary = "Mark a lending record as fully paid")
    public ResponseEntity<LendingDTO> closeLending(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.closeLending(id));
    }
}
