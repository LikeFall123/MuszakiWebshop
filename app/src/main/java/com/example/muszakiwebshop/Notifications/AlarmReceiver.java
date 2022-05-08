package com.example.muszakiwebshop.Notifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.muszakiwebshop.Notifications.WebShopNotification;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        new WebShopNotification(context).send("Ideje vásárolni");
    }
}