package com.finanscepte.settings.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_settings")
public class UserSettings {
    @Id
    private String id;
    private String userId;
    private boolean darkMode;
    private boolean notificationsEnabled;
    private boolean emailSummary;
    private boolean twoFactorEnabled;
    private boolean currencyAlertsEnabled;
    private boolean weeklyReportEnabled;
    private String language;
    private String currency;
}
