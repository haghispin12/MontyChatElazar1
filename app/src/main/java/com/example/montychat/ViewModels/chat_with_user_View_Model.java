package com.example.montychat.ViewModels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.montychat.models.User;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class chat_with_user_View_Model extends ViewModel {

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private String conversionId = null;




    public void sendMessage(@NonNull String messageText, String capturedImage, User receiverUser, String conversionId, PreferenceManager preferenceManager) {
        HashMap<String, Object> message = new HashMap<>();
        if (!messageText.isEmpty()) {
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            message.put(Constants.KEY_MESSAGE, messageText);
            message.put(Constants.KEY_IMAGE_MESSAGE, capturedImage);
            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

            if (conversionId != null) {
                updateConversion(messageText, conversionId,preferenceManager);
            } else {
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_LAST_MESSAGE, messageText);
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                conversion.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
                conversion.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_EMAIL,receiverUser.email);
                conversion.put(Constants.KEY_SENDER_EMAIL,preferenceManager.getString(Constants.KEY_EMAIL));
                addConversion(conversion);
            }
        }
    }



    public void listenMessages(String receiverUserId, String currentUserId, EventListener<QuerySnapshot>eventListener) {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, currentUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUserId)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUserId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, currentUserId)
                .addSnapshotListener(eventListener);
    }



    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message, String conversionId,PreferenceManager preferenceManager) {
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(Constants.KEY_LAST_MESSAGE, message, Constants.KEY_TIMESTAMP, new Date());
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    public void updateAllConversationsWithNewDetails(PreferenceManager preferenceManager) {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        String newName = preferenceManager.getString(Constants.KEY_NAME);
        String newEmail = preferenceManager.getString(Constants.KEY_EMAIL);
        String newImage = preferenceManager.getString(Constants.KEY_IMAGE);

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        DocumentReference documentReference = documentSnapshot.getReference();
                        documentReference.update(Constants.KEY_SENDER_NAME, newName,
                                Constants.KEY_SENDER_EMAIL, newEmail,
                                Constants.KEY_SENDER_IMAGE, newImage);
                    }
                });

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        DocumentReference documentReference = documentSnapshot.getReference();
                        documentReference.update(Constants.KEY_RECEIVER_NAME, newName,
                                Constants.KEY_RECEIVER_EMAIL, newEmail,
                                Constants.KEY_RECEIVER_IMAGE, newImage);
                    }
                });
    }

}
