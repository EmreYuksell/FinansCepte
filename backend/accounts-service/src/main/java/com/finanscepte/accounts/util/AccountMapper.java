package com.finanscepte.accounts.util;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;
import com.finanscepte.accounts.model.Account;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AccountMapper {

    public Account toEntity(AccountRequest request) {
        return Account.builder()
                .userId(request.userId())
                .name(request.name())
                .type(Account.AccountType.valueOf(request.type()))
                .institution(request.institution())
                .balance(request.balance())
                .currency(request.currency() != null ? request.currency() : "TRY")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getName(),
                account.getType().name(),
                account.getInstitution(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public void updateEntity(Account account, AccountRequest request) {
        account.setName(request.name());
        account.setType(Account.AccountType.valueOf(request.type()));
        account.setInstitution(request.institution());
        account.setBalance(request.balance());
        if (request.currency() != null) account.setCurrency(request.currency());
        account.setUpdatedAt(LocalDateTime.now());
    }
}
