package com.example.healthcarechatbot.classes;

import java.util.ArrayList;

public class Response {
    private final String message;
    private final String type;
    private final String list_content;
    private final ArrayList<String> list;

    public Response(String message, String type, String list_content, ArrayList<String> list) {
        this.message = message;
        this.type = type;
        this.list = list;
        this.list_content = list_content;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getList_content() {
        return list_content;
    }

    public ArrayList<String> getList() {
        return list;
    }
}
