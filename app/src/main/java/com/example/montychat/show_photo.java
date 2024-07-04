package com.example.montychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import io.getstream.photoview.PhotoView;

public class show_photo extends AppCompatActivity {

    ImageButton back;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        PhotoView photoView = findViewById(R.id.photoView);
        back = findViewById(R.id.back_button_s_p);

        getWindow().setStatusBarColor(ContextCompat.getColor(show_photo.this, R.color.black));


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("url");

        if (imageUrl != null) {
            Picasso.get().load(imageUrl).into(photoView);
        }
    }
}
