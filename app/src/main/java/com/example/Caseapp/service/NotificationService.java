package com.example.Caseapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.Caseapp.R;

public class NotificationService extends Service {
    private static final String CHANNEL_ID = "CHANNEL_ID";
    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create BroadcastReceiver to receive broadcasted intent from Database Monitoring Service
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("database_change_intent")) {
                    // Extract notification data from the received intent
                    String title = intent.getStringExtra("title");
                    String body = intent.getStringExtra("body");

                    // Call method to send FCM notification
                    sendNotificationToUser(title, body);
                }
            }
        };
        // Register the BroadcastReceiver to listen for the specified action
        IntentFilter filter = new IntentFilter("database_change_intent");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver when the service is destroyed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    // Method to send notification using FCM
    private void sendNotificationToUser(String title, String body) {
        // Construct FCM message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Create the notification to display as foreground service
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("Monitoring database for changes")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();

        // Start the service as a foreground service
        startForeground(1, notification);
    }
}


