package com.flashdash.activity.service.api;


import com.flashdash.activity.config.JwtManager;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.notification.ApiClient;
import com.p4r1nc3.flashdash.notification.ApiException;
import com.p4r1nc3.flashdash.notification.api.NotificationsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.flashdash.activity.exception.ErrorCode.E500001;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JwtManager jwtManager;
    private final UserContext userContext;

    public NotificationService(JwtManager jwtManager,
                               UserContext userContext) {
        this.jwtManager = jwtManager;
        this.userContext = userContext;
    }

    private NotificationsApi getNotificationsApi() {
        String userFrn = userContext.getUserFrn();
        String userEmail = userContext.getUserEmail();
        ApiClient apiClient = new ApiClient();
        String token = jwtManager.generateToken(userFrn, userEmail);
        apiClient.setBearerToken(token);
        return new NotificationsApi(apiClient);
    }

    @Async
    public void sendAchievementUnlockedEmail() {
        NotificationsApi notificationsApi = getNotificationsApi();
        String userFrn = userContext.getUserFrn();
        String userEmail = userContext.getUserEmail();
        try {
            notificationsApi.sendAchievementUnlockedEmail();
            logger.info("Achievement unlocked email sent to user {} with email {}.", userFrn, userEmail);
        } catch (ApiException e) {
            logger.error("Failed to send achievement unlocked email to user {} with email {}. Error: {}", userFrn, userEmail, e.getMessage());
            throw new FlashDashException(E500001, "An error occurred while sending achievement unlocked email. Please try again later.");
        }
    }
}
