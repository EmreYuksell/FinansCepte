package com.finanscepte.goals.repository;

import com.finanscepte.goals.model.Goal;
import com.finanscepte.common.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends GenericRepository<Goal, String> {

    List<Goal> findByUserId(String userId);

    List<Goal> findByUserIdAndCategory(String userId, String category);
}
