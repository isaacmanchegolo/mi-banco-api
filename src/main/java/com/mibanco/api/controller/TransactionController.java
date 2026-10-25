package com.mibanco.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mibanco.api.dto.request.TransferRequest;
import com.mibanco.api.dto.request.DepositRequest;
import com.mibanco.api.dto.response.TransactionResponse;
import com.mibanco.api.service.TransactionService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {
        return ResponseEntity.status(201).body(transactionService.deposit(request, authentication.getName()));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(transactionService.transfer(request, authentication.getName()));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getAccountHistory(
            @PathVariable String accountNumber,
            Authentication authentication) {
        return ResponseEntity.ok(transactionService.getAccountHistory(accountNumber, authentication.getName()));
    }
}
