package com.example.healthcarechatbot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthcarechatbot.R;
import com.example.healthcarechatbot.classes.Message;

import java.util.ArrayList;

public class ChatAdapter extends ArrayAdapter<String> {

    Context context;
    ArrayList<String> arrayList;
    ArrayList<Message> messageArrayList;

    public ChatAdapter(@NonNull Context context, ArrayList<String> arrayList, ArrayList<Message> messageArrayList) {
        super(context, R.layout.lv_chat_left, arrayList);

        this.context = context;
        this.arrayList = arrayList;
        this.messageArrayList = messageArrayList;

    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view;

        if (messageArrayList.get(position).getShowButton()) {
            view = layoutInflater.inflate(R.layout.lv_chat_button, null, true);
            Button button = view.findViewById(R.id.pickSymptoms);
            button.setOnClickListener(v -> {
                Log.i("CLICK", "Button clicked");

            });


        } else if (messageArrayList.get(position).getLeft()) {
            view = layoutInflater.inflate(R.layout.lv_chat_left, null, true);

            TextView textView = view.findViewById(R.id.text_view_chat_left);
            textView.setText(messageArrayList.get(position).getText());

        } else {
            view = layoutInflater.inflate(R.layout.lv_chat_right, null, true);

            TextView textView = view.findViewById(R.id.text_view_chat_right);
            textView.setText(messageArrayList.get(position).getText());
        }

        return view;
    }
}
