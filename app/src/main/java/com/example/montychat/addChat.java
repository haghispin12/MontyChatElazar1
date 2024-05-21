package com.example.montychat;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.montychat.adapters.UserAdapter;
import com.example.montychat.listeners.UserListener;
import com.example.montychat.models.User;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class addChat extends AppCompatActivity implements UserListener {




    private PreferenceManager preferenceManager;
    ImageButton backButton;
    ProgressBar progressBar;
    TextView errorMassage;
    androidx.recyclerview.widget.RecyclerView userRecyclerview;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chat);


        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar_A);
        errorMassage = findViewById(R.id.errorMassage);
        userRecyclerview = findViewById(R.id.userRecyclerview);

        preferenceManager = new PreferenceManager(getApplicationContext());






        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addChat.this.onBackPressed();
            }
        });
        getUsers();
    }

    private void showToast (String s ){
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void getUsers (){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null ){
                        ArrayList<User> users = new ArrayList<User>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size() > 0 ){
                            UserAdapter userAdapter = new UserAdapter(getApplicationContext(),users,this);
                            userRecyclerview.setAdapter(userAdapter);
                            userRecyclerview.setVisibility(View.VISIBLE);

                        } else {
                            shoeErrorMassage();
                        }
                    } else {
                        shoeErrorMassage();
                    }
                });
    }


    private void shoeErrorMassage () {
        errorMassage.setText(String.format("%s","No user available"));
        errorMassage.setVisibility(View.VISIBLE);
    }


    private void loading (Boolean isLoading){
        if(isLoading){
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void onUserClicked (User user) {
        Intent intent = new Intent(getApplicationContext(), chat_with_user.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}
