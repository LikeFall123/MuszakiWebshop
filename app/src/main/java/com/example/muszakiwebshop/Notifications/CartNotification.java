package com.example.muszakiwebshop.Notifications;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompatSideChannelService;

import com.example.muszakiwebshop.R;
import com.example.muszakiwebshop.WebShopActivity;

public class CartNotification {

    private static  String CHANNEL_ID = "cart_notificaito_channel";
    private final int NOTIFICATIONN_ID =0;

    private NotificationManager mNotificationManagar;
    private Context mContext;

    public CartNotification(Context context) {
        this.mContext=context;
        this.mNotificationManagar = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            return;
        }

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "WebShop Notification",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.WHITE);
        channel.setDescription("Notification from WebShop application");

        this.mNotificationManagar.createNotificationChannel(channel);
    }

    public void send(String message){

        Intent intent = new Intent(mContext, WebShopActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,NOTIFICATIONN_ID,intent,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentText(message+" terméket megvetted!")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent);
        this.mNotificationManagar.notify(NOTIFICATIONN_ID,builder.build());
    }

    public void cancel(){
        this.mNotificationManagar.cancel(NOTIFICATIONN_ID);
    }
}

