package com.finanscepte.accounts.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Hesap Entity — Kullanıcının banka/yatırım hesaplarını temsil eder.
 * Generic pattern: AbstractGenericService<Account, String> üzerinden CRUD.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "accounts")
public class Account {

    @Id
    private String id;

    @NotBlank(message = "Kullanıcı ID boş olamaz")
    private String userId;

    @NotBlank(message = "Hesap adı boş olamaz")
    private String name;

    @NotNull(message = "Hesap tipi boş olamaz")
    private AccountType type;

    /** Banka / kurum adı (örn. "Ziraat Bankası") */
    private String institution;

    @Builder.Default
    private double balance = 0.0;

    @Builder.Default
    private String currency = "TRY";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum AccountType {
        VADESIZ, BIRIKIM, YATIRIM, KREDI_KARTI
    }
}
