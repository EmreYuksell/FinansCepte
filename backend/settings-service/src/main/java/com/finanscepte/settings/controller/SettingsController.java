package com.finanscepte.settings.controller;

import com.finanscepte.settings.model.UserProfile;
import com.finanscepte.settings.model.UserSettings;
import com.finanscepte.settings.repository.UserProfileRepository;
import com.finanscepte.settings.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final UserSettingsRepository userSettingsRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public SettingsController(
            UserSettingsRepository userSettingsRepository,
            UserProfileRepository userProfileRepository,
            @Value("${finanscepte.user-service-url:http://localhost:8081}") String userServiceUrl) {
        this.userSettingsRepository = userSettingsRepository;
        this.userProfileRepository = userProfileRepository;
        this.userServiceUrl = userServiceUrl;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping
    public ResponseEntity<UserSettings> getSettings(@RequestParam String userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId);
        if (settings == null) {
            settings = UserSettings.builder()
                    .userId(userId)
                    .darkMode(true)
                    .notificationsEnabled(true)
                    .emailSummary(false)
                    .twoFactorEnabled(false)
                    .currencyAlertsEnabled(true)
                    .weeklyReportEnabled(true)
                    .language("tr")
                    .currency("TRY")
                    .build();
            userSettingsRepository.save(settings);
        }
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<UserSettings> updateSettings(@RequestBody UserSettings settings) {
        return ResponseEntity.ok(userSettingsRepository.save(settings));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(@RequestParam String userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile == null) {
            profile = UserProfile.builder()
                    .userId(userId)
                    .fullName("Kullanıcı")
                    .email("user@ceptefinans.com")
                    .phone("+90 555 123 45 67")
                    .build();
            userProfileRepository.save(profile);
        }
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(@RequestBody UserProfile profile) {
        return ResponseEntity.ok(userProfileRepository.save(profile));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (userId == null || userId.isBlank()
                || oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("userId, oldPassword ve newPassword zorunludur");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Yeni şifre en az 6 karakter olmalıdır");
        }

        try {
            restTemplate.postForEntity(
                    userServiceUrl + "/api/users/" + userId + "/change-password",
                    Map.of("oldPassword", oldPassword, "newPassword", newPassword),
                    Void.class);
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException ex) {
            return ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportData(@RequestParam String userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId);
        UserProfile profile = userProfileRepository.findByUserId(userId);
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("settings", settings);
        data.put("profile", profile);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importData(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> settingsMap = (Map<String, Object>) body.get("settings");
        if (settingsMap != null) {
            UserSettings settings = UserSettings.builder()
                    .userId((String) settingsMap.get("userId"))
                    .darkMode((boolean) settingsMap.getOrDefault("darkMode", false))
                    .notificationsEnabled((boolean) settingsMap.getOrDefault("notificationsEnabled", true))
                    .emailSummary((boolean) settingsMap.getOrDefault("emailSummary", false))
                    .twoFactorEnabled((boolean) settingsMap.getOrDefault("twoFactorEnabled", false))
                    .currencyAlertsEnabled((boolean) settingsMap.getOrDefault("currencyAlertsEnabled", true))
                    .weeklyReportEnabled((boolean) settingsMap.getOrDefault("weeklyReportEnabled", true))
                    .language((String) settingsMap.getOrDefault("language", "tr"))
                    .currency((String) settingsMap.getOrDefault("currency", "TRY"))
                    .build();
            userSettingsRepository.save(settings);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> profileMap = (Map<String, Object>) body.get("profile");
        if (profileMap != null) {
            UserProfile profile = UserProfile.builder()
                    .userId((String) profileMap.get("userId"))
                    .fullName((String) profileMap.getOrDefault("fullName", "Kullanıcı"))
                    .email((String) profileMap.getOrDefault("email", ""))
                    .phone((String) profileMap.getOrDefault("phone", ""))
                    .avatarUrl((String) profileMap.getOrDefault("avatarUrl", ""))
                    .build();
            userProfileRepository.save(profile);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/data")
    public ResponseEntity<Void> deleteData(@RequestParam String userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId);
        if (settings != null) userSettingsRepository.delete(settings);
        UserProfile profile = userProfileRepository.findByUserId(userId);
        if (profile != null) userProfileRepository.delete(profile);
        return ResponseEntity.noContent().build();
    }
}
