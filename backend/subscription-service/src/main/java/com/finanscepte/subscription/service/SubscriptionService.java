package com.finanscepte.subscription.service;

import com.finanscepte.subscription.dto.SubscriptionRequest;
import com.finanscepte.subscription.dto.SubscriptionResponse;

import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse create(SubscriptionRequest request);

    SubscriptionResponse update(String id, SubscriptionRequest request);

    SubscriptionResponse findById(String id);

    List<SubscriptionResponse> findAll();

    List<SubscriptionResponse> findByUserId(String userId);

    void cancel(String id);

    void deleteById(String id);
}
