package com.finanscepte.accounts.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

/**
 * Varlık (Asset) Entity — Hisse, kripto, altın, döviz gibi finansal varlıklar.
 * Kar/zarar = (currentValue - purchaseValue) * quantity şeklinde hesaplanabilir.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "assets")
public class Asset {

    @Id
    private String id;

    @NotBlank(message = "Kullanıcı ID boş olamaz")
    private String userId;

    @NotBlank(message = "Varlık adı boş olamaz")
    private String name;

    @NotNull(message = "Varlık tipi boş olamaz")
    private AssetType type;

    /** Anlık piyasa değeri (1 birim) */
    private double currentValue;

    /** Alış fiyatı (1 birim) */
    private double purchaseValue;

    @Positive(message = "Miktar pozitif olmalıdır")
    private double quantity;

    @Builder.Default
    private String currency = "TRY";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AssetType {
        HISSE, KRIPTO, ALTIN, DOVIZ, DIGER
    }

    /** Toplam kar/zarar hesabı */
    public double getProfitLoss() {
        return (currentValue - purchaseValue) * quantity;
    }

    /** Toplam anlık değer */
    public double getTotalValue() {
        return currentValue * quantity;
    }
}
