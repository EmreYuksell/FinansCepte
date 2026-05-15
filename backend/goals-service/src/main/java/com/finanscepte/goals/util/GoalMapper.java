package com.finanscepte.goals.util;

import com.finanscepte.goals.dto.GoalRequest;
import com.finanscepte.goals.dto.GoalResponse;
import com.finanscepte.goals.model.Goal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GoalMapper {

    public Goal toEntity(GoalRequest request) {
        return Goal.builder()
                .userId(request.userId())
                .name(request.name())
                .targetAmount(request.targetAmount())
                .currentAmount(request.currentAmount())
                .deadline(request.deadline())
                .color(request.color())
                .category(request.category())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public GoalResponse toResponse(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getProgressPercent(),
                goal.getDeadline(),
                goal.getColor(),
                goal.getCategory(),
                goal.getCreatedAt()
        );
    }

    public void updateEntity(Goal goal, GoalRequest request) {
        goal.setName(request.name());
        goal.setTargetAmount(request.targetAmount());
        goal.setCurrentAmount(request.currentAmount());
        goal.setDeadline(request.deadline());
        goal.setColor(request.color());
        goal.setCategory(request.category());
    }
}
