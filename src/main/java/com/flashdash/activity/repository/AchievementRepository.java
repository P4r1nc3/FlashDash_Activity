package com.flashdash.activity.repository;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, String> {
    Optional<Achievement> findByUserFrnAndAchievementTypeAndLevel(String userFrn, AchievementType type, int level);
    Optional<Achievement> findByAchievementFrnAndUserFrn(String achievementFrn, String userFrn);
    List<Achievement> findByUserFrn(String userFrn);
    List<Achievement> findByUserFrnOrderByAchievedAtDesc(String userFrn);
}