package com.example.montychat.adapters;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<chatMessage> conversion;
    private static final int SENDER_VIEW_TYPE = 1;
    private static final int RECEIVER_VIEW_TYPE = 2;
    private static final int SENDER_IMAGE_TYPE = 3;
    private static final int RECEIVER_IMAGE_TYPE = 4;
    private static final int SENDER_EMOJI_HEART_TYPE = 5;
    private static final int RECEIVER_EMOJI_HEART_TYPE = 6;
    private static final int DATE_VIEW_TYPE = 7;
    private final String userId;

    public chatAdapter(Context context, List<chatMessage> messageList) {
        this.context = context;
        this.conversion = messageList;
        userId = new PreferenceManager(context).getString(Constants.KEY_USER_ID);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || isNewDayWithMessages(position)) {
            return DATE_VIEW_TYPE;
        }

        chatMessage message = conversion.get(position);

        if (message.senderId.equals(userId) && (message.message.equals("❤️") || message.message.equals("✌️"))) {
            return SENDER_EMOJI_HEART_TYPE;
        } else if (message.receiverId.equals(userId) && (message.message.equals("❤️") || message.message.equals("✌️"))) {
            return RECEIVER_EMOJI_HEART_TYPE;
        } else if (message.senderId.equals(userId)) {
            if (message.ImageMessageChat != null) {
                return SENDER_IMAGE_TYPE;
            } else {
                return SENDER_VIEW_TYPE;
            }
        } else if (message.receiverId.equals(userId)) {
            if (message.ImageMessageChat != null) {
                return RECEIVER_IMAGE_TYPE;
            } else {
                return RECEIVER_VIEW_TYPE;
            }
        } else {
            return -1; // Error case, should not happen
        }
    }

    private boolean isNewDayWithMessages(int position) {
        if (position == 0) return true;
        chatMessage currentMessage = conversion.get(position);
        chatMessage previousMessage = null;

        // Find previous message that is not a date
        for (int i = position - 1; i >= 0; i--) {
            if (conversion.get(i).message != null) {
                previousMessage = conversion.get(i);
                break;
            }
        }

        if (previousMessage == null) return true;
        return !isSameDay(currentMessage.dateObject, previousMessage.dateObject);
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder - viewType: " + viewType);
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_sent_message, parent, false);
            return new SenderMessageViewHolder(view);
        } else if (viewType == RECEIVER_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_received_message, parent, false);
            return new ReceiverMessageViewHolder(view);
        } else if (viewType == SENDER_EMOJI_HEART_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_heart_emoji_sender, parent, false);
            return new Sender_Emoji_Heart(view);
        } else if (viewType == RECEIVER_EMOJI_HEART_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.container_heart_emoji_reciever, parent, false);
            return new Reciever_Emoji_Heart(view);
        } else if (viewType == DATE_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_view, parent, false);
            return new DateViewHolder(view);
        } else {
            Log.e(TAG, "Invalid view type: " + viewType);
            throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        chatMessage message = conversion.get(position);

        if (holder instanceof DateViewHolder) {
            DateViewHolder dateViewHolder = (DateViewHolder) holder;
            dateViewHolder.bind(message.dateObject);
        } else if (holder instanceof SenderMessageViewHolder) {
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
        } else if (holder instanceof Sender_Emoji_Heart) {
            Sender_Emoji_Heart senderEmojiHeart = (Sender_Emoji_Heart) holder;
            senderEmojiHeart.dateTimeText.setText(message.dateTime);
            senderEmojiHeart.dateTimeText.setVisibility(View.VISIBLE);
            senderEmojiHeart.messageText.setText(message.message);
            senderEmojiHeart.messageText.setVisibility(View.VISIBLE);
        } else if (holder instanceof Reciever_Emoji_Heart) {
            Reciever_Emoji_Heart recieverEmojiHeart = (Reciever_Emoji_Heart) holder;
            recieverEmojiHeart.dateTimeText.setText(message.dateTime);
            recieverEmojiHeart.dateTimeText.setVisibility(View.VISIBLE);
            recieverEmojiHeart.messageText.setText(message.message);
            recieverEmojiHeart.messageText.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap getUserImage(String encoded) {
        byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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

    public class Reciever_Emoji_Heart extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public Reciever_Emoji_Heart(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_R_H);
            dateTimeText = itemView.findViewById(R.id.textDateTime_R_H);
        }
    }

    public class Sender_Emoji_Heart extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView dateTimeText;

        public Sender_Emoji_Heart(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.textMessage_S_H);
            dateTimeText = itemView.findViewById(R.id.textDateTime_S_H);
        }
    }

    public class DateViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(Date date) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            dateTextView.setText(dateFormat.format(date));
        }
    }
}
