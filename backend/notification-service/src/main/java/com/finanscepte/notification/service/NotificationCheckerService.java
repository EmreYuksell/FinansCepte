package com.finanscepte.notification.service;

import com.finanscepte.notification.model.Notification;
import com.finanscepte.notification.repository.NotificationRepository;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class NotificationCheckerService {

    private final NotificationRepository notificationRepository;
    private final RestTemplate restTemplate;

    private static final String ACCOUNTS_URL = "http://accounts-service:8088/api/accounts";
    private static final String GOALS_URL = "http://goals-service:8089/api/goals";
    private static final double LOW_BALANCE_THRESHOLD = 100.0;

    public NotificationCheckerService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        this.restTemplate = new RestTemplateBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Scheduled(fixedRate = 300000)
    public void checkAll() {
        checkLowBalances();
        checkGoalMilestones();
    }

    private void checkLowBalances() {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> accounts = restTemplate.getForObject(ACCOUNTS_URL, List.class);
            if (accounts == null) return;
            for (Map<String, Object> acc : accounts) {
                String userId = (String) acc.get("userId");
                String accountName = (String) acc.get("name");
                Object balanceObj = acc.get("balance");
                double balance = toDouble(balanceObj);

                if (balance < LOW_BALANCE_THRESHOLD && alreadyNotified(userId, "LOW_BALANCE:" + accountName)) {
                    createNotification(userId, "LOW_BALANCE",
                            String.format("%s hesabınızın bakiyesi %.2f TL ile eşik değerin altında.", accountName, balance));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void checkGoalMilestones() {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> goals = restTemplate.getForObject(GOALS_URL, List.class);
            if (goals == null) return;
            for (Map<String, Object> goal : goals) {
                String id = (String) goal.get("id");
                String userId = (String) goal.get("userId");
                String name = (String) goal.get("name");
                double target = toDouble(goal.get("targetAmount"));
                double current = toDouble(goal.get("currentAmount"));
                double progress = target > 0 ? (current / target) * 100 : 0;

                if (progress >= 100 && alreadyNotified(userId, "GOAL_COMPLETE:" + id)) {
                    createNotification(userId, "GOAL_COMPLETE",
                            String.format("Tebrikler! \"%s\" hedefinizi tamamladınız!", name));
                } else if (progress >= 50 && progress < 100 && alreadyNotified(userId, "GOAL_HALF:" + id)) {
                    createNotification(userId, "GOAL_HALF",
                            String.format("\"%s\" hedefinizin yarısına ulaştınız! (%%%.0f)", name, progress));
                }
            }
        } catch (Exception ignored) {
        }
    }

    private boolean alreadyNotified(String userId, String dedupKey) {
        List<Notification> existing = notificationRepository.findByUserId(userId);
        return existing.stream().noneMatch(n -> n.getType() != null && n.getType().equals(dedupKey));
    }

    private void createNotification(String userId, String type, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    private double toDouble(Object val) {
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException e) { return 0; }
        }
        return 0;
    }
}
