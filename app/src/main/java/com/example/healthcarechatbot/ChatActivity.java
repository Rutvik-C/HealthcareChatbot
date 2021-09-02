package com.example.healthcarechatbot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private ListView listViewChat;
    private ChatAdapter chatAdapter;
    private TextView textViewMessage;

    private ArrayList<String> arrayList;
    private ArrayList<Message> messageArrayList;

    private Retrofit retrofit;

    private FirebaseUser mUser;


    private void displaySimpleMessage(String message) {
        messageArrayList.add(new Message(message, true, false, null));
        arrayList.add(message);
        chatAdapter.notifyDataSetChanged();

    }

    private void processResponse(Response response) {
        String type = response.getType();

        switch (type) {
            case "simple":
                displaySimpleMessage(response.getMessage());
                break;

            case "list":
                displaySimpleMessage("Woa Imma show you a list");
                break;

        }
    }

    private void sendSimpleMessage(String message) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", message)
                .addFormDataPart("user", mUser.getEmail())
                .addFormDataPart("list_type", "null")
                .addFormDataPart("list", "null")
                .build();

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

        listViewChat = findViewById(R.id.listViewChat);
        textViewMessage = findViewById(R.id.textViewMessage);

        arrayList = new ArrayList<>();
        messageArrayList = new ArrayList<>();

        chatAdapter = new ChatAdapter(this, arrayList, messageArrayList);

        listViewChat.setAdapter(chatAdapter);

        ImageView button = findViewById(R.id.imageViewSend);
        button.setOnClickListener(v -> {
            String text = textViewMessage.getText().toString();

            if (!text.equals("")) {
                textViewMessage.setText("");

                sendSimpleMessage(text);

                messageArrayList.add(new Message(text, false, false, null));
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
