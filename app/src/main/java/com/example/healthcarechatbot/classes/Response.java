package com.example.healthcarechatbot.classes;

import java.util.ArrayList;

public class Response {
    private final String message;
    private final String type;
    private final ArrayList<String> list;

    public Response(String message, String type, ArrayList<String> list) {
        this.message = message;
        this.type = type;
        this.list = list;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public ArrayList<String> getList() {
        return list;
    }
}
