package com.flashdash.activity.repository;

import com.flashdash.activity.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserFrn(String userFrn);
}
