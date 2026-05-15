package com.finanscepte.goals.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "goals")
public class Goal {

    @Id
    private String id;

    @NotBlank(message = "Kullanıcı ID boş olamaz")
    private String userId;

    @NotBlank(message = "Hedef adı boş olamaz")
    private String name;

    @NotNull(message = "Hedef tutarı boş olamaz")
    @Positive(message = "Hedef tutarı pozitif olmalıdır")
    private double targetAmount;

    @NotNull(message = "Mevcut tutar boş olamaz")
    @Positive(message = "Mevcut tutar pozitif olmalıdır")
    private double currentAmount;

    @NotNull(message = "Bitiş tarihi boş olamaz")
    private LocalDate deadline;

    private String color;

    private String category;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public double getProgressPercent() {
        return targetAmount > 0 ? (currentAmount / targetAmount) * 100 : 0;
    }
}
