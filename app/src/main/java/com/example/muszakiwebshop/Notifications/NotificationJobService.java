package com.example.muszakiwebshop.Notifications;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class NotificationJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        new WebShopNotification(getApplicationContext())
                .send("Ideje vásárolni!");

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}
