package com.finanscepte.accounts.service;

import com.finanscepte.accounts.dto.AccountRequest;
import com.finanscepte.accounts.dto.AccountResponse;
import com.finanscepte.accounts.model.Account;
import com.finanscepte.accounts.repository.AccountRepository;
import com.finanscepte.accounts.service.impl.AccountServiceImpl;
import com.finanscepte.accounts.util.AccountMapper;
import com.finanscepte.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private AccountMapper accountMapper;
    @InjectMocks private AccountServiceImpl service;

    @Test
    void create_shouldSaveAndReturn() {
        AccountRequest req = new AccountRequest("u1", "Vadesiz", "BANK", "Ziraat", 1000, "TRY");
        Account entity = Account.builder().userId("u1").name("Vadesiz").build();
        AccountResponse resp = new AccountResponse("1", "u1", "Vadesiz", "BANK", "Ziraat", 1000, "TRY", LocalDateTime.now(), null);
        when(accountMapper.toEntity(req)).thenReturn(entity);
        when(accountRepository.save(entity)).thenReturn(entity);
        when(accountMapper.toResponse(entity)).thenReturn(resp);

        AccountResponse result = service.create(req);
        assertThat(result.name()).isEqualTo("Vadesiz");
        verify(accountRepository).save(entity);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(accountRepository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUserId_shouldReturnFiltered() {
        Account entity = Account.builder().userId("u1").build();
        AccountResponse resp = new AccountResponse("1", "u1", "A", "BANK", null, 0, "TRY", null, null);
        when(accountRepository.findByUserId("u1")).thenReturn(List.of(entity));
        when(accountMapper.toResponse(entity)).thenReturn(resp);

        assertThat(service.findByUserId("u1")).hasSize(1);
    }
}
