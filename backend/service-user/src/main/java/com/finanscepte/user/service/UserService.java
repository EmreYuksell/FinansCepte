package com.finanscepte.user.service;

import com.finanscepte.common.GenericService;
import com.finanscepte.user.dto.UserRequest;
import com.finanscepte.user.dto.UserResponse;

public interface UserService extends GenericService<UserResponse, String> {

    UserResponse createUser(UserRequest request);

    UserResponse findByEmail(String email);
}
