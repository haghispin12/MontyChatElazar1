package com.example.montychat.firebase;


import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MessagingService extends FirebaseMessagingService{

    public  void onNewToken (@NonNull String token){
        super.onNewToken(token);


    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

//        String title = remoteMessage.getNotification().getTitle();
//        String message = remoteMessage.getNotification().getBody();
//
//        // שלח התראה
//        NotificationHelper.sendNotification(this, title, message);
    }
}
