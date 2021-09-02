package com.example.healthcarechatbot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthcarechatbot.ChatActivity;
import com.example.healthcarechatbot.R;
import com.example.healthcarechatbot.classes.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends ArrayAdapter<String> {

    Context context;
    ArrayList<String> arrayList;
    ArrayList<Message> messageArrayList;
    ListView symptomsList;

    public ChatAdapter(@NonNull Context context, ArrayList<String> arrayList, ArrayList<Message> messageArrayList, ListView symptomsList) {
        super(context, R.layout.lv_chat_left, arrayList);

        this.context = context;
        this.arrayList = arrayList;
        this.messageArrayList = messageArrayList;
        this.symptomsList = symptomsList;

    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view;

        if (messageArrayList.get(position).getArrayListSuggestions() != null) {
            view = layoutInflater.inflate(R.layout.lv_chat_list, null, true);

            // TODO: populate list here
            ArrayList<String> symptoms = messageArrayList.get(position).getArrayListSuggestions();
            symptomsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            symptomsList.setItemsCanFocus(false);

            symptomsList.setOnItemClickListener((parent1, view1, position1, id) -> {
                CheckedTextView foo = (CheckedTextView) view;
                if(foo.isChecked()) {
                    ChatActivity.symptomsSuggestions.add(symptoms.get(position1));
                }
                else {
                    if(ChatActivity.symptomsSuggestions.contains(symptoms.get(position))) {
                      ChatActivity.symptomsSuggestions.remove(symptoms.get(position));
                    }
                }
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
