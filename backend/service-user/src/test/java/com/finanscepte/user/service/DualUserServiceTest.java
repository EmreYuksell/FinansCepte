package com.finanscepte.user.service;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.common.exception.UnauthorizedException;
import com.finanscepte.user.dto.LoginRequest;
import com.finanscepte.user.dto.UserRequest;
import com.finanscepte.user.dto.UserResponse;
import com.finanscepte.user.model.User;
import com.finanscepte.user.repository.UserMongoRepository;
import com.finanscepte.user.util.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DualUserServiceTest {

    @Mock private UserMongoRepository userMongoRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private DualUserService dualUserService;

    @Test
    void createUser_shouldSaveViaMongo() {
        UserRequest req = new UserRequest("Test", "test@mail.com", "123456");
        User user = User.builder().name("Test").email("test@mail.com").password("123456").build();
        UserResponse resp = new UserResponse("1", "Test", "test@mail.com", null, null);
        when(userMapper.toEntity(req)).thenReturn(user);
        when(userMongoRepository.save(any())).thenReturn(user);
        when(userMapper.toResponse(any())).thenReturn(resp);

        UserResponse result = dualUserService.createUser(req);
        assertThat(result.email()).isEqualTo("test@mail.com");
    }

    @Test
    void findByEmail_shouldThrow_whenNotFound() {
        when(userMongoRepository.findByEmail("x@x.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> dualUserService.findByEmail("x@x.com")).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        User user = User.builder().id("1").name("Test").email("test@mail.com").build();
        UserResponse resp = new UserResponse("1", "Test", "test@mail.com", null, null);
        when(userMongoRepository.findById("1")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(resp);

        Optional<UserResponse> result = dualUserService.findById("1");
        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Test");
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordWrong() {
        User user = User.builder().email("a@b.com").password("$2a$10$encoded").build();
        when(userMongoRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> dualUserService.login(new LoginRequest("a@b.com", "yanlis")))
                .isInstanceOf(UnauthorizedException.class);
    }
}
