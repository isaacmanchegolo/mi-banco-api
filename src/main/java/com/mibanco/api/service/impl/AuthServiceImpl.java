package com.mibanco.api.service.impl;

import java.math.BigDecimal;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mibanco.api.dto.request.LoginRequest;
import com.mibanco.api.dto.request.RegisterRequest;
import com.mibanco.api.dto.response.AuthResponse;
import com.mibanco.api.entity.Account;
import com.mibanco.api.entity.User;
import com.mibanco.api.entity.enums.AccountType;
import com.mibanco.api.entity.enums.Role;
import com.mibanco.api.exception.BadRequestException;
import com.mibanco.api.repository.AccountRepository;
import com.mibanco.api.repository.UserRepository;
import com.mibanco.api.security.JwtTokenProvider;
import com.mibanco.api.service.AuthService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("El email ya se encuentra registrado");
        }

        // 1. Crear el usuario
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        // 2. Crear una cuenta bancaria por defecto para el usuario
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .balance(new BigDecimal("0.00"))
                .accountType(AccountType.SAVINGS)
                .user(savedUser)
                .build();

        accountRepository.save(account);

        // 3. Autenticar y retornar token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Usuario no encontrado"));

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private String generateAccountNumber() {
                String accountNumber;
                do {
                accountNumber = "2001" + String.format("%08d", RANDOM.nextInt(100_000_000));
                } while (accountRepository.existsByAccountNumber(accountNumber));
                return accountNumber;
    }
}
