package com.example.montychat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.montychat.ViewModels.chat_with_user_View_Model;
import com.example.montychat.adapters.chatAdapter;
import com.example.montychat.models.User;
import com.example.montychat.models.chatMessage;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class chat_with_user extends AppCompatActivity  {

    private User receiverUser;
    private List<chatMessage> chatMessages;
    private chatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversionId = null;


    //from her designs variable


    TextView textName;
    androidx.appcompat.widget.AppCompatImageView imageBack;
    androidx.appcompat.widget.AppCompatImageView infoButton;
    androidx.appcompat.widget.AppCompatImageView imageProfile;
    androidx.recyclerview.widget.RecyclerView adapter;
    ImageButton CameraButton;
    EditText inputMessage;
    FrameLayout layoutSend;
    ProgressBar progsesBar_Chat;
    private String capturedImage;
    View view_back;
    View ViewPass;
    Button btnGallery;
    Button btnCamera;

    //up to her designs variable

    chat_with_user_View_Model vm;
   // private  EventListener<QuerySnapshot> eventListener;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_with_user);

        //assign each item to its activity
        textName = findViewById(R.id.textName_chat);
        imageBack = findViewById(R.id.image_back);
        adapter = findViewById(R.id.chatRecyclerView);
        inputMessage = findViewById(R.id.input_message);
        layoutSend = findViewById(R.id.layout_send);
        progsesBar_Chat = findViewById(R.id.progsesBar_Chat);
        CameraButton = findViewById(R.id.CameraButton);
        infoButton = findViewById(R.id.image_info);
        view_back = findViewById(R.id.view_back);
        ViewPass = findViewById(R.id.ViewPass);
        btnGallery = findViewById(R.id.btnGallery);
        btnCamera = findViewById(R.id.btnCamera);


        vm = new ViewModelProvider(this).get(chat_with_user_View_Model.class);

        setListeners();
        loadReceiverDetails();
        init();
        //eventListener = vm.buildEventListener(chatMessages,chatAdapter,adapter,progsesBar_Chat,preferenceManager.getString(Constants.KEY_USER_ID),receiverUser,conversionId);

        vm.listenMessages(preferenceManager.getString(Constants.KEY_USER_ID),receiverUser.id,eventListener);

    }
    private void init (){//function that prepare the activity to be used/.

        preferenceManager = new PreferenceManager(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new chatAdapter((Context) this,chatMessages);
        adapter.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }



    private final EventListener<QuerySnapshot> eventListener = (value, error)->{
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
            checkForConversion();
        }
    };


    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        textName.setText(receiverUser.name);
    }
    private void setListeners (){

        layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vm.sendMessage(inputMessage.getText().toString(),capturedImage,receiverUser,conversionId,preferenceManager);
                inputMessage.setText("");
            }
        });
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chat_with_user.this,showUser.class);
                intent.putExtra(Constants.KEY_USER,receiverUser);
                startActivity(intent);
            }
        });
    }

    private String getReadableDateTime (Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void showToast (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void checkForConversion () {
        if(chatMessages.size() != 0){
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
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

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener =task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId =documentSnapshot.getId();
        }
    };


}