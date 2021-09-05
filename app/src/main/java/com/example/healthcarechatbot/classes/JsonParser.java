package com.example.healthcarechatbot.classes;

import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser {
    private HashMap<String, String> parseJsonObject(JSONObject object) {
        HashMap<String, String> dataList = new HashMap<>();

        try {
            String name = object.getJSONObject("venue").getString("name");
            String lat = object.getJSONObject("venue").getJSONObject("location").getString("lat");
            String lng = object.getJSONObject("venue").getJSONObject("location").getString("lng");
            String dist = object.getJSONObject("venue").getJSONObject("location").getString("distance");

            dataList.put("name", name);
            dataList.put("lat", lat);
            dataList.put("lng", lng);
            dataList.put("distance", dist);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    private List<HashMap<String, String>> parseJsonArray(JSONArray jsonArray) {
        List<HashMap<String, String>> dataList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                HashMap<String, String> hashMap = parseJsonObject((JSONObject) jsonArray.get(i));

                dataList.add(hashMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return dataList;
    }

    public List<HashMap<String, String>> parseResults(JSONObject object) {
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;

        try {
            jsonObject = object.getJSONObject("response");
            jsonArray = jsonObject.getJSONArray("groups");
            jsonObject = (JSONObject) jsonArray.get(0);  // first element of groups
            jsonArray = jsonObject.getJSONArray("items");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return parseJsonArray(jsonArray);
    }
}
