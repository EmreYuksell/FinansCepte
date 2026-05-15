package com.finanscepte.settings.repository;

import com.finanscepte.settings.model.UserProfile;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends GenericRepository<UserProfile, String> {
    UserProfile findByUserId(String userId);
}
