package com.example.montychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.montychat.utilities.Constants;

public class showImage extends AppCompatActivity {

    ImageView imageView;
    androidx.appcompat.widget.AppCompatImageView backButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        imageView = findViewById(R.id.imageViewShowImage);
        backButton = findViewById(R.id.image_back_show_Image);

        Intent intent = getIntent();

        // Retrieve the value using the key
        String imageMessage = intent.getStringExtra(Constants.KEY_IMAGE_MESSAGE);

        // Now you can use the value as needed
        if (imageMessage != null) {
            imageView.setImageBitmap(getUserImage_R(imageMessage));
        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImage.this.onBackPressed();
                }
        });
    }
    private Bitmap getUserImage_R (String encoded){
        byte [] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}