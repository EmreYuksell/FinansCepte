package com.finanscepte.settings.repository;

import com.finanscepte.settings.model.UserSettings;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingsRepository extends GenericRepository<UserSettings, String> {
    UserSettings findByUserId(String userId);
}
