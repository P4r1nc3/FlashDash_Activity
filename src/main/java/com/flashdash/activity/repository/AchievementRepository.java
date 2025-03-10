package com.flashdash.activity.repository;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, String> {
    List<Achievement> findByUserFrn(String userFrn);
    Optional<Achievement> findByUserFrnAndAchievementTypeAndLevel(String userFrn, AchievementType type, int level);
}
