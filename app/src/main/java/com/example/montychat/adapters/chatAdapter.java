package com.example.montychat.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.montychat.R;
import com.example.montychat.models.chatMessage;
import com.example.montychat.utilities.Constants;
import com.example.montychat.utilities.PreferenceManager;

import java.util.List;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<chatMessage> conversion;
    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;
    private static final int SENDER_EMOJI_HEART_TYPE = 3;
    private static final int RECEIVER_EMOJI_HEART_TYPE = 4;
    private final String userId;

    public chatAdapter(Context context, List<chatMessage> messageList) {
        this.context = context;
        this.conversion = messageList;
        userId = new PreferenceManager(context).getString(Constants.KEY_USER_ID);
    }

    public List<chatMessage> getConversion() {
        return conversion;
    }

    @Override
    public int getItemViewType(int position) {
        chatMessage message = conversion.get(position);
        if (message.message != null) {
            if (message.senderId.equals(userId) && (message.message.equals("❤️") || message.message.equals("✌️"))) {
                return SENDER_EMOJI_HEART_TYPE;
            } else if (message.receiverId.equals(userId) && (message.message.equals("❤️") || message.message.equals("✌️"))) {
                return RECEIVER_EMOJI_HEART_TYPE;
            } else if (message.senderId.equals(userId)) {
                return SENDER_VIEW_TYPE;
            } else if (message.receiverId.equals(userId)) {
                return RECEIVER_VIEW_TYPE;
            } else {
                return -1; // Error case, should not happen
            }
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("chatAdapter", "onCreateViewHolder - viewType: " + viewType);
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_sent_message, parent, false);
            return new SenderMessageViewHolder(view);
        } else if (viewType == RECEIVER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_received_message, parent, false);
            return new ReceiverMessageViewHolder(view);
        } else if (viewType == SENDER_EMOJI_HEART_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_heart_emoji_sender, parent, false);
            return new SenderEmojiHeartViewHolder(view);
        } else if (viewType == RECEIVER_EMOJI_HEART_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_heart_emoji_reciever, parent, false);
            return new ReceiverEmojiHeartViewHolder(view);
        }
        return null;
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
        } else if (holder instanceof SenderEmojiHeartViewHolder) {
            SenderEmojiHeartViewHolder senderEmojiHeart = (SenderEmojiHeartViewHolder) holder;
            senderEmojiHeart.dateTimeText.setText(message.dateTime);
            senderEmojiHeart.dateTimeText.setVisibility(View.VISIBLE);
            senderEmojiHeart.messageText.setText(message.message);
            senderEmojiHeart.messageText.setVisibility(View.VISIBLE);
        } else if (holder instanceof ReceiverEmojiHeartViewHolder) {
            ReceiverEmojiHeartViewHolder receiverEmojiHeart = (ReceiverEmojiHeartViewHolder) holder;
            receiverEmojiHeart.dateTimeText.setText(message.dateTime);
            receiverEmojiHeart.dateTimeText.setVisibility(View.VISIBLE);
            receiverEmojiHeart.messageText.setText(message.message);
            receiverEmojiHeart.messageText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return conversion.size();
    }

    public class SenderMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public SenderMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_S);
            dateTimeText = itemView.findViewById(R.id.textDateTime_S);
        }

        public void setSize(int num) {
            messageText.setTextSize(num);
        }
    }

    public class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public ReceiverMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_R);
            dateTimeText = itemView.findViewById(R.id.textDateTime_R);
        }

        public void setSize(int num) {
            messageText.setTextSize(num);
        }
    }

    public class ReceiverEmojiHeartViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public ReceiverEmojiHeartViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_R_H);
            dateTimeText = itemView.findViewById(R.id.textDateTime_R_H);
        }
    }

    public class SenderEmojiHeartViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public SenderEmojiHeartViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_S_H);
            dateTimeText = itemView.findViewById(R.id.textDateTime_S_H);
        }
    }
}
