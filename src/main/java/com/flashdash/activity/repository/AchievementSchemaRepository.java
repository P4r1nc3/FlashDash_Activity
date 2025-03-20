package com.flashdash.activity.repository;

import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementSchemaRepository extends JpaRepository<AchievementSchema, String> {
    Optional<AchievementSchema> findBySchemaFrn(String schemaFrn);
    Optional<AchievementSchema> findByAchievementTypeAndLevel(AchievementType achievementType, int level);
    List<AchievementSchema> findByAchievementType(AchievementType achievementType);
    List<AchievementSchema> findByAchievementTypeOrderByLevelAsc(AchievementType achievementType);
}