package com.example.healthcarechatbot;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.healthcarechatbot.adapters.ChatAdapter;
import com.example.healthcarechatbot.classes.Message;
import com.example.healthcarechatbot.serverconnection.ApiInterface;
import com.example.healthcarechatbot.classes.Response;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private ListView listViewChat;
    private ChatAdapter chatAdapter;
    @SuppressLint("StaticFieldLeak")
    public static TextView textViewMessage;

    private Boolean listSend;
    private String listType;

    private ArrayList<String> arrayList;
    private ArrayList<Message> messageArrayList;

    public static ArrayList<String> symptomsSelected;

    private Retrofit retrofit;

    private FirebaseUser mUser;

    private void displaySimpleMessage(String message) {
        messageArrayList.add(new Message(message, true, null));
        arrayList.add(message);
        chatAdapter.notifyDataSetChanged();
    }

    private void showListMessage(ArrayList<String> symptoms) {
        messageArrayList.add(new Message(null, null, symptoms));
        arrayList.add("__message__");
        chatAdapter.notifyDataSetChanged();

    }

    private void processResponse(Response response) {
        String type = response.getType();

        if (response.getMessage() != null) {
            String[] strings = response.getMessage().split("__n__");

            for (String s : strings) {
                displaySimpleMessage(s);

            }
        }

        if (type.equals("list")) {
            listSend = true;
            listType = response.getList_content();

            showListMessage(response.getList());

        }
    }

    private void callAPI(RequestBody requestBody) {
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<Response> mCall = apiInterface.sendMessage(requestBody);

        mCall.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                Response res = response.body();

                if (res != null) {
                    processResponse(res);

                } else {
                    displaySimpleMessage("Hmm my head is not working for the moment\nTry after some time while I grab a coffee!");
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                displaySimpleMessage("Oh no! we have an error: " + t.getMessage());

            }
        });

    }

    private void sendListMessage(String type) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", "null")
                .addFormDataPart("user", mUser.getEmail())
                .addFormDataPart("list_type", type)
                .addFormDataPart("list", symptomsSelected.toString())
                .build();

        callAPI(requestBody);
        symptomsSelected.clear();

    }

    private void sendSimpleMessage(String message) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", message)
                .addFormDataPart("user", mUser.getEmail())
                .addFormDataPart("list_type", "null")
                .addFormDataPart("list", "null")
                .build();

        callAPI(requestBody);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        symptomsSelected = new ArrayList<>();

        listViewChat = findViewById(R.id.listViewChat);
        textViewMessage = findViewById(R.id.textViewMessage);

        arrayList = new ArrayList<>();
        messageArrayList = new ArrayList<>();

        chatAdapter = new ChatAdapter(this, arrayList, messageArrayList);
        listViewChat.setAdapter(chatAdapter);

        listSend = false;

        ImageView button = findViewById(R.id.imageViewSend);
        button.setOnClickListener(v -> {
            String text = textViewMessage.getText().toString();

            Log.i("VAR", "listsend" + listSend + " | listtype " + listType);

            if (!text.equals("")) {
                textViewMessage.setText("");

                if (listSend) {
                    if (listType.equals("symptoms")) {
                        sendListMessage("symptom_list");
                    } else if (listType.equals("additional")) {
                        sendListMessage("add_symptom_list");
                    }

                } else {
                    sendSimpleMessage(text);

                }

                messageArrayList.add(new Message(text, false, null));
                arrayList.add(text);
                chatAdapter.notifyDataSetChanged();

            }

        });

        retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
