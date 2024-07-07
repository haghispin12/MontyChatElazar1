package com.example.montychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.montychat.adapters.RecentConversationAdapter;
import com.example.montychat.listeners.ConversionListener;
import com.example.montychat.models.User;
import com.example.montychat.models.chatMessage;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    TextView textName;
    com.makeramen.roundedimageview.RoundedImageView imageProfile;
    androidx.appcompat.widget.AppCompatImageView signOutButton;
    ImageButton addButton;
    androidx.recyclerview.widget.RecyclerView conversationRecyclerView;
    ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private List<chatMessage> chatMessages;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;
    String token;
    String otherToken;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textName = findViewById(R.id.textName);
        imageProfile = findViewById(R.id.imageProfile);
        signOutButton = findViewById(R.id.imageSignOut);
        addButton = findViewById(R.id.addButton);
        conversationRecyclerView = findViewById(R.id.conversationRecyclerView);
        progressBar = findViewById(R.id.progsesBar_M);

        preferenceManager = new PreferenceManager(getApplicationContext());

        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.input_back2));


        init();
        loadUserDetails();
        getToken();
        setLissners();
        listenConversation();


    }


    private void init() {
        chatMessages = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(this, chatMessages, this);
        conversationRecyclerView.setAdapter(conversationAdapter); // Here is the issue

        database = FirebaseFirestore.getInstance();
    }


    private void loadUserDetails() {
        textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        Picasso.get().load(preferenceManager.getString(Constants.KEY_IMAGE)).into(imageProfile);

    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void listenConversation() {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }


    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage chatMessage = new chatMessage();

                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.conversationEmail = documentChange.getDocument().getString(Constants.KEY_RECEIVER_EMAIL);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.conversationEmail = documentChange.getDocument().getString(Constants.KEY_SENDER_EMAIL);
                    }

                    if(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE) != null){
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    } else chatMessage.message = "photo";
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    // Add new message to the beginning of the list
                    chatMessages.add(0, chatMessage);
                }else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < chatMessages.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if ((chatMessages.get(i).senderId.equals(senderId) && chatMessages.get(i).receiverId.equals(receiverId)) ||
                                (chatMessages.get(i).senderId.equals(receiverId) && chatMessages.get(i).receiverId.equals(senderId))) {
                            if(documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE) != null){
                                chatMessages.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            } else chatMessages.get(i).message = "photo";
                            chatMessages.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }

            }

            // Sort the list based on dateObject (assuming it's a Date object)
            Collections.sort(chatMessages, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));

            // Notify adapter of dataset change
            conversationAdapter.notifyDataSetChanged();

            // Scroll to the top
            conversationRecyclerView.smoothScrollToPosition(0);

            // Make RecyclerView visible and hide progress bar
            conversationRecyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    };



    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token) {
        this.token = token;
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void signOut() {
        showToast("signing out");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), log_In.class));
                    //finish();
                })
                .addOnFailureListener(e -> {
                    showToast("Unable to sign out");
                });
    }

    private void setLissners() {
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), addChat.class);
                startActivity(intent);
            }
        });
        textName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, settings.class);
                intent.putExtra(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                intent.putExtra(Constants.KEY_EMAIL, preferenceManager.getString(Constants.KEY_EMAIL));
                intent.putExtra(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onConversionListener(User user) {
        database.collection("users").document(user.id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        otherToken = document.getString(Constants.KEY_FCM_TOKEN);
                        user.token = otherToken;
                    }
                }
            }
        });
        Intent chatIntent = new Intent(getApplicationContext(), chat_with_user.class);
        chatIntent.putExtra(Constants.KEY_USER, user);
        chatIntent.putExtra("token", token);
        startActivity(chatIntent);
        //finish();
    }
}
