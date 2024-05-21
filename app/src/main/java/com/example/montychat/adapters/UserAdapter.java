package com.example.montychat.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.montychat.R;
import com.example.montychat.listeners.UserListener;
import com.example.montychat.models.User;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private ArrayList<User> userList;
    private final UserListener userListener;

    public UserAdapter(Context context, ArrayList<User> userList, UserListener userListener) {
        this.context = context;
        this.userList = userList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_container, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.userPicture.setImageBitmap(getUserImage(user.image));
        holder.userName.setText(user.name);
        holder.userEmail.setText(user.email);

        holder.setUserData(userList.get(position));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        com.makeramen.roundedimageview.RoundedImageView userPicture;
        TextView userName;
        TextView userEmail;
        View view;
        User user;

         UserViewHolder(View itemView) {
            super(itemView);
            userPicture = itemView.findViewById(R.id.imageProfile);
            userName = itemView.findViewById(R.id.textName_I);
            userEmail = itemView.findViewById(R.id.textEmail_I);
            User user = new User();
            user.name = userName.getText().toString();
            user.email = userEmail.getText().toString();
            user.image = userPicture.toString();
            this.user =user;
            itemView.getRootView().setOnClickListener(v -> userListener.onUserClicked(user));


            view = itemView;

        }
        void setUserData (User user){
             userName.setText(user.name);
             userEmail.setText(user.email);
             userPicture.setImageBitmap(getUserImage(user.image));
             view.getRootView().setOnClickListener(v-> userListener.onUserClicked(user));
        }
    }
    private Bitmap getUserImage (String encoded){
        byte [] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
