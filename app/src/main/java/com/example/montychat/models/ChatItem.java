package com.example.montychat.models;

import java.util.Date;

public class ChatItem {
    public static final int TYPE_DATE = 0;
    public static final int TYPE_MESSAGE = 1;

    private int itemType;
    private chatMessage message;
    private Date date;

    public ChatItem(chatMessage message) {
        this.itemType = TYPE_MESSAGE;
        this.message = message;
    }

    public ChatItem(Date date) {
        this.itemType = TYPE_DATE;
        this.date = date;
    }

    public int getItemType() {
        return itemType;
    }

    public chatMessage getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }
}
