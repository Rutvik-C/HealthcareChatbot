package com.example.healthcarechatbot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthcarechatbot.ChatActivity;
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

    public float convertDpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;

    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view;

        if (messageArrayList.get(position).getArrayListSuggestions() != null) {
            view = layoutInflater.inflate(R.layout.lv_chat_list, null, true);

            ArrayList<String> suggestions = messageArrayList.get(position).getArrayListSuggestions();
            if (!suggestions.contains("None")) {
                suggestions.add("None");
            }
            SuggestionsAdapter adapter = new SuggestionsAdapter(context, suggestions);

            ListView listView = view.findViewById(R.id.symptomsListView);
            listView.setAdapter(adapter);

            RelativeLayout relativeLayout = view.findViewById(R.id.parentRelativeLayout);
            RelativeLayout.LayoutParams layoutDescription = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) convertDpToPx(context, suggestions.size() * 47));
            relativeLayout.setLayoutParams(layoutDescription);

            listView.setOnItemClickListener((parent1, view1, i, id) -> {

                if (ChatActivity.symptomsSelected.contains(suggestions.get(i))) {
                    ChatActivity.symptomsSelected.remove(suggestions.get(i));

                } else {
                    if (suggestions.get(i).equals("None")) {
                        ChatActivity.symptomsSelected.clear();

                    } else {
                        ChatActivity.symptomsSelected.add(suggestions.get(i));

                    }
                }

                StringBuilder s = new StringBuilder();
                s.append("Symptoms: ");

                for (int j = 0; j < ChatActivity.symptomsSelected.size(); j++) {
                    s.append(ChatActivity.symptomsSelected.get(j));

                    if (j == ChatActivity.symptomsSelected.size() - 2) {
                        s.append(", and ");
                    } else if (j != ChatActivity.symptomsSelected.size() - 1) {
                        s.append(", ");
                    }
                }
                ChatActivity.textViewMessage.setText(s.toString());

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
