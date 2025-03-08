package com.flashdash.activity.repository;


import com.flashdash.activity.model.Activity;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserFrn(String userFrn);
    List<Activity> findByActivity(ActivityTypeEnum activity);
}
