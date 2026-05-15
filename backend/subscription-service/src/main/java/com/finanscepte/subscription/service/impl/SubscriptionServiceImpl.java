package com.finanscepte.subscription.service.impl;

import com.finanscepte.common.exception.ResourceNotFoundException;
import com.finanscepte.subscription.dto.SubscriptionRequest;
import com.finanscepte.subscription.dto.SubscriptionResponse;
import com.finanscepte.subscription.model.Subscription;
import com.finanscepte.subscription.repository.SubscriptionRepository;
import com.finanscepte.subscription.service.SubscriptionService;
import com.finanscepte.subscription.util.SubscriptionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, SubscriptionMapper subscriptionMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public SubscriptionResponse create(SubscriptionRequest request) {
        Subscription subscription = subscriptionMapper.toEntity(request);
        Subscription saved = subscriptionRepository.save(subscription);
        return subscriptionMapper.toResponse(saved);
    }

    @Override
    public SubscriptionResponse update(String id, SubscriptionRequest request) {
        Subscription existing = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        existing.setProductId(request.productId());
        existing.setStartDate(request.startDate());
        existing.setEndDate(request.endDate());
        existing.setAmount(request.amount());
        existing.setUpdatedAt(LocalDateTime.now());
        Subscription updated = subscriptionRepository.save(existing);
        return subscriptionMapper.toResponse(updated);
    }

    @Override
    public SubscriptionResponse findById(String id) {
        return subscriptionRepository.findById(id)
                .map(subscriptionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
    }

    @Override
    public List<SubscriptionResponse> findAll() {
        return subscriptionRepository.findAll().stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    public List<SubscriptionResponse> findByUserId(String userId) {
        return subscriptionRepository.findByUserId(userId).stream()
                .map(subscriptionMapper::toResponse)
                .toList();
    }

    @Override
    public void cancel(String id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
        subscription.setStatus("CANCELLED");
        subscription.setUpdatedAt(LocalDateTime.now());
        subscriptionRepository.save(subscription);
    }

    @Override
    public void deleteById(String id) {
        if (!subscriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subscription", "id", id);
        }
        subscriptionRepository.deleteById(id);
    }
}
