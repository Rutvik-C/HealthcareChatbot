package com.example.healthcarechatbot.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.healthcarechatbot.R;
import com.example.healthcarechatbot.classes.Message;

import java.util.ArrayList;

public class SuggestionsAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> arrayList;

    public SuggestionsAdapter(@NonNull Context context, ArrayList<String> arrayList) {
        super(context, R.layout.lv2_suggestions, arrayList);

        this.context = context;
        this.arrayList = arrayList;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.lv2_suggestions, null, true);

        TextView textView = view.findViewById(R.id.textViewSymptom);
        textView.setText(arrayList.get(position));

        return view;
    }
}
