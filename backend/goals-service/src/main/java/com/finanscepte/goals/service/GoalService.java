package com.finanscepte.goals.service;

import com.finanscepte.goals.dto.GoalRequest;
import com.finanscepte.goals.dto.GoalResponse;

import java.util.List;

public interface GoalService {

    GoalResponse create(GoalRequest request);

    GoalResponse update(String id, GoalRequest request);

    GoalResponse findById(String id);

    List<GoalResponse> findAll();

    List<GoalResponse> findByUserId(String userId);

    List<GoalResponse> findByUserIdAndCategory(String userId, String category);

    GoalResponse deposit(String id, double amount);

    void deleteById(String id);
}
