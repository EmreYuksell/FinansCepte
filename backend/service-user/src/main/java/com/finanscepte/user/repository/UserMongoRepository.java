package com.finanscepte.user.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMongoRepository extends GenericRepository<User, String> {

    Optional<User> findByEmail(String email);
}
