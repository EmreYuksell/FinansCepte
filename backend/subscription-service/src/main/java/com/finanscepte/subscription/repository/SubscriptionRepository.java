package com.finanscepte.subscription.repository;

import com.finanscepte.common.GenericRepository;
import com.finanscepte.subscription.model.Subscription;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends GenericRepository<Subscription, String> {

    List<Subscription> findByUserId(String userId);

    List<Subscription> findByStatus(String status);
}
