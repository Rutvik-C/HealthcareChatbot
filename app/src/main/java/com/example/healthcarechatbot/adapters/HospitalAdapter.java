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

public class HospitalAdapter extends ArrayAdapter<String> {

    Context context;
    ArrayList<String> arrayListName;
    ArrayList<String> arrayListDistance;

    public HospitalAdapter(@NonNull Context context, ArrayList<String> arrayListName, ArrayList<String> arrayListDistance) {
        super(context, R.layout.lv_hospitals, arrayListName);

        this.context = context;
        this.arrayListName = arrayListName;
        this.arrayListDistance = arrayListDistance;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.lv_hospitals, null, true);

        TextView textViewName = view.findViewById(R.id.textViewName);
        textViewName.setText(arrayListName.get(position));

        TextView textViewDistance = view.findViewById(R.id.textViewDistance);
        textViewDistance.setText(arrayListDistance.get(position));

        return view;

    }
}
