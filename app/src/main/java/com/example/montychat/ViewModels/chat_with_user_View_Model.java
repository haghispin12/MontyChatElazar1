package com.example.montychat.ViewModels;

import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.montychat.adapters.chatAdapter;
import com.example.montychat.models.User;
import com.example.montychat.models.chatMessage;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class chat_with_user_View_Model extends ViewModel {

    private final FirebaseFirestore database = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<chatMessage>> chatMessagesLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private String conversionId = null;


    public LiveData<List<chatMessage>> getChatMessages() {
        return chatMessagesLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

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
                updateConversion(messageText, conversionId);
            } else {
                HashMap<String, Object> conversion = new HashMap<>();
                conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversion.put(Constants.KEY_LAST_MESSAGE, messageText);
                conversion.put(Constants.KEY_TIMESTAMP, new Date());
                addConversion(conversion);
            }
        }
    }

    private void updateConversion(String message, String conversionId) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    public void listenMessages(String senderId, String receiverId,EventListener<QuerySnapshot> eventListener) {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, senderId)
                .addSnapshotListener(eventListener);
    }

    public EventListener<QuerySnapshot> buildEventListener (List<chatMessage> chatMessages, chatAdapter chatAdapter, androidx.recyclerview.widget.RecyclerView adapter, ProgressBar progsesBar_Chat,String senderId,User receiverUser,String conversionId2){

        conversionId = conversionId2;
         final EventListener<QuerySnapshot> eventListener = (value, error)->{
            if(error != null){
                return;
            }
            if(value != null){
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if(documentChange.getType() == DocumentChange.Type.ADDED){
                        chatMessage chatMessage = new chatMessage();
                        chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                        chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        chatMessage.ImageMessageChat = documentChange.getDocument().getString(Constants.KEY_IMAGE_MESSAGE);
                        chatMessages.add(chatMessage);
                    }
                }
                Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
                if(chatAdapter.getItemCount() == 0){
                    chatAdapter.notifyDataSetChanged();
                } else {
                    chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                    adapter.smoothScrollToPosition(chatMessages.size());
                }
                adapter.setVisibility(View.VISIBLE);
            }
            progsesBar_Chat.setVisibility(View.GONE);
            if(conversionId == null){
                checkForConversion(chatMessages,receiverUser,senderId);
            }
        };
        return eventListener;
    }


    private void checkForConversion (List<chatMessage> chatMessages,User receiverUser,String senderId) {
                if(chatMessages.size() != 0){
                    checkForConversionRemotely(
                            senderId,
                            receiverUser.id
            );

            checkForConversionRemotely(
                    receiverUser.id,
                    senderId
            );
        }
    }

    private void checkForConversionRemotely (String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId =documentSnapshot.getId();
        }
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    public String getConversionId() {
        return conversionId;
    }
}
