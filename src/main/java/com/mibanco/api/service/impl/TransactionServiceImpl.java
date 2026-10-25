package com.mibanco.api.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mibanco.api.dto.request.TransferRequest;
import com.mibanco.api.dto.request.DepositRequest;
import com.mibanco.api.dto.response.TransactionResponse;
import com.mibanco.api.entity.Account;
import com.mibanco.api.entity.Transaction;
import com.mibanco.api.entity.enums.TransactionType;
import com.mibanco.api.exception.BadRequestException;
import com.mibanco.api.exception.InsufficientBalanceException;
import com.mibanco.api.exception.ResourceNotFoundException;
import com.mibanco.api.repository.AccountRepository;
import com.mibanco.api.repository.TransactionRepository;
import com.mibanco.api.service.TransactionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public TransactionResponse deposit(DepositRequest request, String currentUserEmail) {
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + request.getAccountNumber()));

        if (!account.getUser().getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("La cuenta no pertenece al usuario autenticado");
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        Transaction deposit = Transaction.builder()
                .destinationAccount(account)
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .description(request.getDescription() == null || request.getDescription().isBlank()
                        ? "Depósito de prueba" : request.getDescription())
                .build();

        accountRepository.save(account);
        return mapToResponse(transactionRepository.save(deposit));
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request, String currentUserEmail) {
        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new BadRequestException("La cuenta de origen y la de destino no pueden ser la misma");
        }

        // Bloquea siempre en el mismo orden para proteger los saldos ante transferencias simultáneas.
        String firstNumber = request.getSourceAccountNumber().compareTo(request.getDestinationAccountNumber()) < 0
                ? request.getSourceAccountNumber() : request.getDestinationAccountNumber();
        String secondNumber = firstNumber.equals(request.getSourceAccountNumber())
                ? request.getDestinationAccountNumber() : request.getSourceAccountNumber();
        Account first = accountRepository.findByAccountNumberForUpdate(firstNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + firstNumber));
        Account second = accountRepository.findByAccountNumberForUpdate(secondNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + secondNumber));
        Account sourceAccount = firstNumber.equals(request.getSourceAccountNumber()) ? first : second;
        Account destinationAccount = firstNumber.equals(request.getDestinationAccountNumber()) ? first : second;

        if (!sourceAccount.getUser().getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("La cuenta de origen no pertenece al usuario autenticado");
        }

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar la transferencia");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction transaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER)
                .description(request.getDescription() == null || request.getDescription().isBlank() ? "Transferencia entre cuentas" : request.getDescription())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountHistory(String accountNumber, String currentUserEmail) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + accountNumber));

        if (!account.getUser().getEmail().equals(currentUserEmail)) {
            throw new BadRequestException("No tiene permisos para consultar el historial de esta cuenta");
        }

        return transactionRepository.findAllByAccountId(account.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .sourceAccountNumber(transaction.getSourceAccount() != null ? transaction.getSourceAccount().getAccountNumber() : null)
                .destinationAccount(transaction.getDestinationAccount() != null ? transaction.getDestinationAccount().getAccountNumber() : null)
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
