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
import com.example.montychat.listeners.ConversionListener;
import com.example.montychat.models.User;
import com.example.montychat.models.chatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> {

    private final Context context;
    private final List<chatMessage> conversations;
    private final ConversionListener listener;

    public RecentConversationAdapter(Context context, List<chatMessage> conversations, ConversionListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_container_recent, parent, false);
        return new ConversionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        chatMessage conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public class ConversionViewHolder extends RecyclerView.ViewHolder {
        private final com.makeramen.roundedimageview.RoundedImageView imageProfile;
        private final TextView textName;
        private final TextView textRecentMessage;


        public ConversionViewHolder(View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageProfile_R);
            textName = itemView.findViewById(R.id.textName_R);
            textRecentMessage = itemView.findViewById(R.id.textRecentMessage_R);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    chatMessage conversation = conversations.get(position);
                    User user = new User();
                    user.image = conversation.conversionImage;
                    user.name = conversation.conversionName;
                    user.id = conversation.conversionId;
                    listener.onConversionListener(user);
                }
            });
        }

        public void bind(chatMessage conversation) {
            textName.setText(conversation.conversionName);
            textRecentMessage.setText(conversation.message);
            imageProfile.setImageBitmap(getUserImage_R(conversation.conversionImage));
            // Format the date and time
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(conversation.dateObject);
        }
        private Bitmap getUserImage_R (String encoded){
            byte [] bytes = Base64.decode(encoded, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
    }
}
