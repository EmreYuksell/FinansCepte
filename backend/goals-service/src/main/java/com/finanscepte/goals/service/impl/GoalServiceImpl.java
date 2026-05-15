package com.finanscepte.goals.service.impl;

import com.finanscepte.goals.dto.GoalRequest;
import com.finanscepte.goals.dto.GoalResponse;
import com.finanscepte.goals.model.Goal;
import com.finanscepte.goals.repository.GoalRepository;
import com.finanscepte.goals.service.GoalService;
import com.finanscepte.goals.util.GoalMapper;
import com.finanscepte.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;

    public GoalServiceImpl(GoalRepository goalRepository, GoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
    }

    @Override
    public GoalResponse create(GoalRequest request) {
        Goal goal = goalMapper.toEntity(request);
        Goal saved = goalRepository.save(goal);
        return goalMapper.toResponse(saved);
    }

    @Override
    public GoalResponse update(String id, GoalRequest request) {
        Goal existing = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        goalMapper.updateEntity(existing, request);
        Goal updated = goalRepository.save(existing);
        return goalMapper.toResponse(updated);
    }

    @Override
    public GoalResponse findById(String id) {
        return goalRepository.findById(id)
                .map(goalMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
    }

    @Override
    public List<GoalResponse> findAll() {
        return goalRepository.findAll().stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    public List<GoalResponse> findByUserId(String userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    public List<GoalResponse> findByUserIdAndCategory(String userId, String category) {
        return goalRepository.findByUserIdAndCategory(userId, category).stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    public GoalResponse deposit(String id, double amount) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        goal.setCurrentAmount(goal.getCurrentAmount() + amount);
        Goal updated = goalRepository.save(goal);
        return goalMapper.toResponse(updated);
    }

    @Override
    public void deleteById(String id) {
        if (!goalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Goal", "id", id);
        }
        goalRepository.deleteById(id);
    }
}
