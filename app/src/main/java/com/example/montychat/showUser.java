package com.example.montychat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.montychat.models.User;
import com.example.montychat.utilities.Constants;

public class showUser extends AppCompatActivity {

    User receiverUser;
    TextView email,name;
    Button backToChat;
    com.makeramen.roundedimageview.RoundedImageView profile;
    ImageView temp;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user);

        getWindow().setStatusBarColor(ContextCompat.getColor(showUser.this,R.color.input_back2));


        email = findViewById(R.id.userEmailInfo);
        name = findViewById(R.id.userNameInfo);
        backToChat = findViewById(R.id.buttonInfo);
        profile = findViewById(R.id.imageProfile_I);
        temp = findViewById(R.id.tempererySrc);

        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        if(receiverUser.name != null){
            name.setText(receiverUser.name);
        }
        if(receiverUser.image != null){
            profile.setImageBitmap(getUserImage_R(receiverUser.image));
            temp.setVisibility(View.GONE);
        }

        if(receiverUser.email != null){
            email.setText(receiverUser.email);
        }else email.setText("email is null");
        backToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    private Bitmap getUserImage_R (String encoded){
        byte [] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}