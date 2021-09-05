package com.example.healthcarechatbot;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthcarechatbot.adapters.ChatAdapter;
import com.example.healthcarechatbot.classes.Message;
import com.example.healthcarechatbot.classes.Response;
import com.example.healthcarechatbot.serverconnection.ApiInterface;
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

    private ChatAdapter chatAdapter;
    @SuppressLint("StaticFieldLeak")
    public static TextView textViewMessage;

    private LinearLayout linearLayout;

    private Boolean listSend;
    private String listType;

    private SQLiteDatabase ChatDatabase;

    private ArrayList<String> arrayList;
    private ArrayList<Message> messageArrayList;

    public static ArrayList<String> symptomsSelected;

    private Retrofit retrofit;

    private FirebaseUser mUser;

    private void displaySimpleMessage(String message, Boolean isLeft) {
        messageArrayList.add(new Message(message, isLeft, null));
        arrayList.add(message);
        chatAdapter.notifyDataSetChanged();

        if (arrayList.size() != 0) {
            linearLayout.setVisibility(View.GONE);

        }
    }

    private void saveMessageInDatabase(String message, Boolean isLeft) {
        message = message.replace('"', ' ');
        message = message.replace('\'', ' ');

        if (isLeft) {
            ChatDatabase.execSQL("INSERT INTO chat VALUES ('" + message + "', 1)");

        } else {
            ChatDatabase.execSQL("INSERT INTO chat VALUES ('" + message + "', 0)");

        }
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
                displaySimpleMessage(s, true);
                saveMessageInDatabase(s, true);

            }
        }

        if (type.equals("list")) {
            listSend = true;
            listType = response.getList_content();

            showListMessage(response.getList());

        }

        if (type.equals("map")) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);

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
                    displaySimpleMessage("Hmm my head is not working for the moment", true);
                    saveMessageInDatabase("Hmm my head is not working for the moment", true);

                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                displaySimpleMessage("Oh no! we have an error: " + t.getMessage(), true);
                saveMessageInDatabase("Oh no! we have an error" + t.getMessage(), true);

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
        listSend = false;
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

    public void showAccount(View view) {

        View mView = LayoutInflater.from(this).inflate(R.layout.alert_account, null);
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);

        mBuilder.setView(mView);

        final AlertDialog mAlertDialog = mBuilder.create();
        mAlertDialog.show();

        TextView textViewAccount = mView.findViewById(R.id.textViewAccount);
        TextView textViewEmail = mView.findViewById(R.id.textViewEmail);

        String email = mUser.getEmail();
        textViewEmail.setText(email);

        if (email != null) {
            String text = email.charAt(0) + "";
            textViewAccount.setText(text.toUpperCase());
        } else {
            textViewAccount.setText("@");
        }

        Button button = mView.findViewById(R.id.buttonLogout);
        button.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            mAlertDialog.cancel();

            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

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

        symptomsSelected = new ArrayList<>();

        linearLayout = findViewById(R.id.linearLayoutInitial);

        ListView listViewChat = findViewById(R.id.listViewChat);
        textViewMessage = findViewById(R.id.textViewMessage);

        arrayList = new ArrayList<>();
        messageArrayList = new ArrayList<>();

        chatAdapter = new ChatAdapter(this, arrayList, messageArrayList);
        listViewChat.setAdapter(chatAdapter);

        listSend = false;

        TextView textViewAccount = findViewById(R.id.textViewAccount);
        String email = mUser.getEmail();
        if (email != null) {
            String text = email.charAt(0) + "";
            textViewAccount.setText(text.toUpperCase());
        } else {
            textViewAccount.setText("@");
        }

        ChatDatabase = this.openOrCreateDatabase("ChatDatabase", MODE_PRIVATE, null);
        ChatDatabase.execSQL("CREATE TABLE IF NOT EXISTS chat (text VARCHAR(100), is_left INT(1))");

        // Read database
        Cursor cursor = ChatDatabase.rawQuery("SELECT * FROM chat", null);

        int textColumnIndex = cursor.getColumnIndex("text");
        int isLeftColumnIndex = cursor.getColumnIndex("is_left");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String text = cursor.getString(textColumnIndex);
            int isLeft = cursor.getInt(isLeftColumnIndex);

            displaySimpleMessage(text, isLeft == 1);

            cursor.moveToNext();
        }

        cursor.close();

        ImageView button = findViewById(R.id.imageViewSend);
        button.setOnClickListener(v -> {
            String text = textViewMessage.getText().toString();

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

                displaySimpleMessage(text, false);
                saveMessageInDatabase(text, false);

            }

        });

        retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
