package com.example.montychat;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.montychat.adapters.chatAdapter;
import com.example.montychat.models.User;
import com.example.montychat.models.chatMessage;
import com.example.montychat.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class SentImageChat extends AppCompatActivity {


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap capturedImage = data.getParcelableExtra("capturedImage");
            String message = data.getStringExtra("message");

        }



    }
    private static final int CAMERA_REQUEST_CODE = 122;
    PreferenceManager preferenceManager;
    private User receiverUser;
    private chatAdapter chatAdapter;

    ImageView imageMessage;
    ImageButton sentImageButton;
    EditText input;
    private List<chatMessage> chatMessages = new ArrayList<>();
    private chatMessage currentImageMessage;
    private Bitmap capturedImage; // המשתנה שיאחסן את התמונה

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_message_chat);

        imageMessage = findViewById(R.id.capturedImage);
        sentImageButton = findViewById(R.id.sentImageButton);
        input = findViewById(R.id.input_message_S);

        // Retrieve the captured image from the intent
        Bitmap capturedImage = getIntent().getParcelableExtra("capturedImage");

        if (capturedImage != null) {
            imageMessage.setImageBitmap(capturedImage);
            imageMessage.setVisibility(View.VISIBLE);
        }
        setOnListenner();
    }

        // Function to send the image and text as a message

        private void setOnListenner (){
            sentImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //sentMessage();
                }
            });
        }
}


