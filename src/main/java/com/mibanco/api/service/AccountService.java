package com.mibanco.api.service;

import java.util.List;

import com.mibanco.api.dto.response.AccountResponse;

public interface AccountService {
    List<AccountResponse> getUserAccounts(String email);
    AccountResponse getAccountByNumber(String accountNumber, String email);

}
