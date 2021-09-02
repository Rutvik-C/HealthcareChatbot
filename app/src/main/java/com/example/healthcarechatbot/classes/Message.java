package com.example.healthcarechatbot.classes;

import java.util.ArrayList;

public class Message {
    private final String text;
    private final Boolean isLeft;
    private final ArrayList<String> arrayListSuggestions;

    public Message(String text, Boolean isLeft, ArrayList<String> arrayListSuggestions) {
        this.text = text;
        this.isLeft = isLeft;
        this.arrayListSuggestions = arrayListSuggestions;
    }

    public String getText() {
        return text;
    }

    public Boolean getLeft() {
        return isLeft;
    }

    public ArrayList<String> getArrayListSuggestions() {
        return arrayListSuggestions;
    }
}
