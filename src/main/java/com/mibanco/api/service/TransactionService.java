package com.mibanco.api.service;

import java.util.List;

import com.mibanco.api.dto.request.TransferRequest;
import com.mibanco.api.dto.request.DepositRequest;
import com.mibanco.api.dto.response.TransactionResponse;

public interface TransactionService {
    TransactionResponse deposit(DepositRequest request, String currentUserEmail);
    TransactionResponse transfer(TransferRequest request, String currentUserEmail);
    List<TransactionResponse> getAccountHistory(String accountNumber, String currentUserEmail);
}
