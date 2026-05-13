package com.finanscepte.transaction.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.transaction.dto.TransactionRequest;
import com.finanscepte.transaction.dto.TransactionResponse;
import com.finanscepte.transaction.model.Transaction;
import com.finanscepte.transaction.repository.TransactionRepository;
import com.finanscepte.transaction.service.impl.TransactionServiceImpl;
import com.finanscepte.transaction.util.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository repository;
    @Mock private TransactionMapper mapper;
    @InjectMocks private TransactionServiceImpl service;

    @Test
    void create_shouldSaveAndReturn() {
        Transaction t = Transaction.builder().userId("u1").amount(BigDecimal.valueOf(100)).type("GELIR").build();
        TransactionRequest req = new TransactionRequest("u1", null, BigDecimal.valueOf(100), "GELIR", "maas");
        TransactionResponse resp = new TransactionResponse("1", "u1", null, BigDecimal.valueOf(100), "GELIR", "maas", null, null);
        when(mapper.toEntity(req)).thenReturn(t);
        when(repository.save(t)).thenReturn(t);
        when(mapper.toResponse(t)).thenReturn(resp);

        TransactionResponse result = service.create(req);
        assertThat(result.type()).isEqualTo("GELIR");
        verify(repository).save(t);
    }

    @Test
    void findByUserId_shouldReturnFiltered() {
        Transaction t = Transaction.builder().userId("u1").build();
        TransactionResponse resp = new TransactionResponse("1", "u1", null, null, null, null, null, null);
        when(repository.findByUserId("u1")).thenReturn(List.of(t));
        when(mapper.toResponse(t)).thenReturn(resp);

        List<TransactionResponse> result = service.findByUserId("u1");
        assertThat(result).hasSize(1);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(repository.findById("99")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("99")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(repository.existsById("99")).thenReturn(false);
        assertThatThrownBy(() -> service.deleteById("99")).isInstanceOf(ResourceNotFoundException.class);
    }
}
