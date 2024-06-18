package com.example.Caseapp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DatabaseMonitoringService extends Service {
    private ValueEventListener valueEventListener;
    private DatabaseReference databaseReference;

    public DatabaseMonitoringService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Create database reference and attach ValueEventListener
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("notifications").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check for changes in the database and trigger notifications as needed
                // Example: send notification using FCM when data changes


                String notificationTitle = "New User accepted";
                String notificationBody = "New User with Id "+dataSnapshot.child("acceptorId")+"and phone number "+ dataSnapshot.child("acceptorPhone")+" has accepted your case.";


                Intent broadcastIntent = new Intent("database_change_intent");
                broadcastIntent.putExtra("title", notificationTitle);
                broadcastIntent.putExtra("body", notificationBody);
                LocalBroadcastManager.getInstance(DatabaseMonitoringService.this)
                        .sendBroadcast(broadcastIntent);

               // sendNotificationToUser("New User accepted", "New User with Id "+dataSnapshot.child("acceptorId")+"and phone number "+ dataSnapshot.child("acceptorPhone")+" has accepted your case." );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        };
        databaseReference.addValueEventListener(valueEventListener);

        // Return START_STICKY to restart the service if it's killed by the system
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove ValueEventListener and stop listening for changes when the service is destroyed
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Method to send notification using FCM

}
