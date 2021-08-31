package com.example.healthcarechatbot.classes;

import java.util.ArrayList;

public class Message {
    private final String text;
    private final Boolean isLeft;
    private final Boolean showButton;
    private final ArrayList<String> arrayListSuggestions;

    public Message(String text, Boolean isLeft, Boolean showButton, ArrayList<String> arrayListSuggestions) {
        this.text = text;
        this.isLeft = isLeft;
        this.showButton = showButton;
        this.arrayListSuggestions = arrayListSuggestions;
    }

    public String getText() {
        return text;
    }

    public Boolean getLeft() {
        return isLeft;
    }

    public Boolean getShowButton() {
        return showButton;
    }

    public ArrayList<String> getArrayListSuggestions() {
        return arrayListSuggestions;
    }
}
