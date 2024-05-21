package com.example.montychat.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.example.montychat.R;
import com.example.montychat.models.chatMessage;
import com.example.montychat.showImage;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<chatMessage> conversion ;
    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;
    private static final int SENDER_IMAGE_TYPE = 3;
    private static final int RECEIVER_IMAGE_TYPE = 4;
    private final String userId;



    public chatAdapter(Context context, List<chatMessage> messageList) {
        this.context = context;
        this.conversion  = messageList;
        userId = new PreferenceManager(context).getString(Constants.KEY_USER_ID);
    }

    @Override
    public int getItemViewType(int position) {
        chatMessage message = conversion.get(position);
        if (message.senderId.equals(userId)) {
            return message.ImageMessageChat != null ? SENDER_IMAGE_TYPE : SENDER_VIEW_TYPE;
        } else {
            return message.ImageMessageChat != null ? RECEIVER_IMAGE_TYPE : RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == SENDER_VIEW_TYPE) {
            view = inflater.inflate(R.layout.item_container_sent_message, parent, false);
            return new SenderMessageViewHolder(view);
        } else if (viewType == RECEIVER_VIEW_TYPE) {
            view = inflater.inflate(R.layout.item_container_received_message, parent, false);
            return new ReceiverMessageViewHolder(view);
        } else if (viewType == SENDER_IMAGE_TYPE) {
            view = inflater.inflate(R.layout.item_container_sent_image, parent, false);
            return new SenderImageViewHolder(view);
        } else if (viewType == RECEIVER_IMAGE_TYPE) {
            view = inflater.inflate(R.layout.item_container_receiv_image, parent, false);
            return new ReceiverImageViewHolder(view);
        }

        // Return a default ViewHolder or handle other view types if needed
        throw new IllegalArgumentException("Unknown viewType: " + viewType);
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        chatMessage message = conversion.get(position);

        if (holder instanceof SenderMessageViewHolder) {
            SenderMessageViewHolder senderViewHolder = (SenderMessageViewHolder) holder;
            senderViewHolder.dateTimeText.setText(message.dateTime);
            senderViewHolder.dateTimeText.setVisibility(View.VISIBLE);

            if (senderViewHolder.messageText != null) {
                senderViewHolder.messageText.setText(message.message);
                senderViewHolder.messageText.setVisibility(View.VISIBLE);
            }

        } else if (holder instanceof ReceiverMessageViewHolder) {
            ReceiverMessageViewHolder receiverMessageViewHolder = (ReceiverMessageViewHolder) holder;
            receiverMessageViewHolder.dateTimeText.setText(message.dateTime);
            receiverMessageViewHolder.dateTimeText.setVisibility(View.VISIBLE);

            if (receiverMessageViewHolder.messageText != null) {
                receiverMessageViewHolder.messageText.setText(message.message);
                receiverMessageViewHolder.messageText.setVisibility(View.VISIBLE);
            }
        }else if(holder instanceof SenderImageViewHolder){
            SenderImageViewHolder senderImageViewHolder = (SenderImageViewHolder) holder;
            senderImageViewHolder.dateTimeText.setText(message.dateTime);
            senderImageViewHolder.dateTimeText.setVisibility(View.VISIBLE);
            senderImageViewHolder.ImageMessage.setImageBitmap(getUserImage(message.ImageMessageChat));
            senderImageViewHolder.ImageMessage.setVisibility(View.VISIBLE);

            if(senderImageViewHolder.messageText != null ){
                senderImageViewHolder.messageText.setText(message.message);
                senderImageViewHolder.messageText.setVisibility(View.VISIBLE);
            }
        }else if(holder instanceof ReceiverImageViewHolder){
            ReceiverImageViewHolder receiverImageViewHolder = (ReceiverImageViewHolder) holder;
            receiverImageViewHolder.dateTimeText.setText(message.dateTime);
            receiverImageViewHolder.dateTimeText.setVisibility(View.VISIBLE);
            receiverImageViewHolder.ImageMessage.setImageBitmap(getUserImage(message.ImageMessageChat));
            receiverImageViewHolder.ImageMessage.setVisibility(View.VISIBLE);

            if(receiverImageViewHolder.messageText != null ){
                receiverImageViewHolder.messageText.setText(message.message);
                receiverImageViewHolder.messageText.setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap getUserImage (String encoded){
        byte [] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }



    @Override
    public int getItemCount() {
        return conversion .size();
    }

    public class SenderMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public SenderMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_S);
            dateTimeText = itemView.findViewById(R.id.textDateTime_S);
        }
    }
    public class SenderImageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;
        ImageView ImageMessage;

        public SenderImageViewHolder(View itemView) {
            super(itemView);
            ImageMessage = itemView.findViewById(R.id.Sent_Message_Image);
            messageText = itemView.findViewById(R.id.textMessage_S_I);
            dateTimeText = itemView.findViewById(R.id.textDateTime_S_I);

                ImageMessage.setOnClickListener(view -> {
                    if(ImageMessage != null && ImageMessage.getVisibility() == View.VISIBLE) {
                        Bitmap bitmap = ((BitmapDrawable) ImageMessage.getDrawable()).getBitmap();
                        Intent intent = new Intent(context, showImage.class);
                        intent.putExtra(Constants.KEY_IMAGE_MESSAGE,bitmapToString(bitmap));
                        context.startActivity(intent);
                    }
                });
            }


        }
    private void showToast (String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
    public String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encodedImage;
    }


    public class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;


        public ReceiverMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_R);
            dateTimeText = itemView.findViewById(R.id.textDateTime_R);

        }
    }
    public class ReceiverImageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;
        ImageView ImageMessage;


        public ReceiverImageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_R_I);
            dateTimeText = itemView.findViewById(R.id.textDateTime_R_I);
            ImageMessage = itemView.findViewById(R.id.Receiver_Message_Image);

            if (ImageMessage != null) {
                ImageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bitmap bitmap = ((BitmapDrawable) ImageMessage.getDrawable()).getBitmap();
                        Intent intent = new Intent(context, showImage.class);
                        intent.putExtra(Constants.KEY_IMAGE_MESSAGE, bitmapToString(bitmap));
                        context.startActivity(intent);
                    }
                });
            }
        }
    }
}
