package com.finanscepte.user.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.user.dto.UserRequest;
import com.finanscepte.user.dto.UserResponse;
import com.finanscepte.user.model.User;
import com.finanscepte.user.model.UserJpaEntity;
import com.finanscepte.user.repository.UserJpaRepository;
import com.finanscepte.user.repository.UserMongoRepository;
import com.finanscepte.user.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DualUserService implements UserService {

    @Autowired(required = false)
    private UserJpaRepository userJpaRepository;

    @Autowired(required = false)
    private UserMongoRepository userMongoRepository;

    @Autowired
    private UserMapper userMapper;

    private boolean isJpaMode() {
        return userJpaRepository != null;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        if (isJpaMode()) {
            UserJpaEntity entity = UserJpaEntity.builder()
                    .name(request.name())
                    .email(request.email())
                    .password(request.password())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            UserJpaEntity saved = userJpaRepository.save(entity);
            return new UserResponse(
                    saved.getId().toString(),
                    saved.getName(),
                    saved.getEmail(),
                    saved.getCreatedAt(),
                    saved.getUpdatedAt()
            );
        } else {
            User user = userMapper.toEntity(request);
            User saved = userMongoRepository.save(user);
            return userMapper.toResponse(saved);
        }
    }

    @Override
    public UserResponse findByEmail(String email) {
        if (isJpaMode()) {
            UserJpaEntity entity = userJpaRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
            return new UserResponse(
                    entity.getId().toString(),
                    entity.getName(),
                    entity.getEmail(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } else {
            User user = userMongoRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
            return userMapper.toResponse(user);
        }
    }

    @Override
    public UserResponse save(UserResponse dto) {
        throw new UnsupportedOperationException("Use createUser instead");
    }

    @Override
    public List<UserResponse> findAll() {
        if (isJpaMode()) {
            return userJpaRepository.findAll().stream()
                    .map(e -> new UserResponse(
                            e.getId().toString(),
                            e.getName(),
                            e.getEmail(),
                            e.getCreatedAt(),
                            e.getUpdatedAt()))
                    .toList();
        } else {
            return userMongoRepository.findAll().stream()
                    .map(userMapper::toResponse)
                    .toList();
        }
    }

    @Override
    public Optional<UserResponse> findById(String id) {
        if (isJpaMode()) {
            return userJpaRepository.findById(Long.valueOf(id))
                    .map(e -> new UserResponse(
                            e.getId().toString(),
                            e.getName(),
                            e.getEmail(),
                            e.getCreatedAt(),
                            e.getUpdatedAt()));
        } else {
            return userMongoRepository.findById(id).map(userMapper::toResponse);
        }
    }

    @Override
    public UserResponse update(String id, UserResponse dto) {
        if (isJpaMode()) {
            UserJpaEntity entity = userJpaRepository.findById(Long.valueOf(id))
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            entity.setName(dto.name());
            entity.setEmail(dto.email());
            entity.setUpdatedAt(LocalDateTime.now());
            UserJpaEntity updated = userJpaRepository.save(entity);
            return new UserResponse(
                    updated.getId().toString(),
                    updated.getName(),
                    updated.getEmail(),
                    updated.getCreatedAt(),
                    updated.getUpdatedAt()
            );
        } else {
            User user = userMapper.toEntity(null);
            user.setId(id);
            user.setName(dto.name());
            user.setEmail(dto.email());
            user.setUpdatedAt(LocalDateTime.now());
            User updated = userMongoRepository.save(user);
            return userMapper.toResponse(updated);
        }
    }

    @Override
    public void deleteById(String id) {
        if (isJpaMode()) {
            userJpaRepository.deleteById(Long.valueOf(id));
        } else {
            userMongoRepository.deleteById(id);
        }
    }
}
