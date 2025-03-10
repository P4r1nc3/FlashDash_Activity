package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FriendsAddedAchievementHandler extends BaseAchievementHandler {

    private static final int[] FRIEND_LEVELS = {3, 10, 25, 50, 100};

    public FriendsAddedAchievementHandler(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalFriends = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.FRIEND_INVITE_RESPONDED)
                .count();

        for (int i = 0; i < FRIEND_LEVELS.length; i++) {
            if (totalFriends >= FRIEND_LEVELS[i]) {
                grantAchievement(userFrn, AchievementType.FRIENDS_ADDED, i + 1);
            }
        }

        checkNext(userFrn, activities);
    }
}
