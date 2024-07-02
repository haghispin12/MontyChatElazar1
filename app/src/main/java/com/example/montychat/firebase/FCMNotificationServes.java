package com.example.montychat.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.montychat.MainActivity;
import com.example.montychat.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMNotificationServes extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 0;
    private static final String CHANNEL_ID = "my_channel_id"; // Replace with your channel ID if needed

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Extract message data
        String message = remoteMessage.getData().get("message");
        String title = remoteMessage.getNotification().getTitle();

        // Show notification
        sendNotification(message, title);
    }

    private void sendNotification(String message, String title) {
        // Create an Intent for the activity to be launched
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("message", message); // Optional: Add data to the Intent

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.designer2) // Replace with your notification icon
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Display notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
