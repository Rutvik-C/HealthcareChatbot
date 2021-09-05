package com.example.healthcarechatbot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.healthcarechatbot.adapters.HospitalAdapter;
import com.example.healthcarechatbot.classes.JsonParser;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Integer LOCATION_REQUEST = 0;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private double currLat, currLong;

    private ListView listViewHospital;

    private List<HashMap<String, String>> hospitals;

    public void back(View view) {
        onBackPressed();

    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>>{
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            JsonParser jsonParser = new JsonParser();

            List<HashMap<String, String>> mapList = null;
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(strings[0]);

                mapList = jsonParser.parseResults(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            hospitals = hashMaps;

            ArrayList<String> arrayListName = new ArrayList<>(), arrayListDistance = new ArrayList<>();
            HospitalAdapter hospitalAdapter = new HospitalAdapter(MapsActivity.this, arrayListName, arrayListDistance);
            listViewHospital.setAdapter(hospitalAdapter);

            for (int i = 0; i < hashMaps.size(); i++) {
                HashMap<String, String> hashMap = hashMaps.get(i);

                arrayListName.add(hashMap.get("name"));
                arrayListDistance.add(hashMap.get("distance") + " m");
                hospitalAdapter.notifyDataSetChanged();

                float lat = Float.parseFloat(hashMap.get("lat"));
                float lng = Float.parseFloat(hashMap.get("lng"));
                LatLng loc = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(loc).title(hashMap.get("name")).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

            }
        }
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = null;

            try {
                data = downloadUrl(strings[0]);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);

        }
    }

    private String downloadUrl(String string) throws IOException {
        URL url = new URL(string);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder builder = new StringBuilder();
        String line = "";

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        reader.close();
        return builder.toString();
    }

    private void onLocationFetched(Location location) {
        if (location != null) {
            currLat = location.getLatitude();
            currLong = location.getLongitude();

            LatLng loc = new LatLng(currLat, currLong);
            mMap.addMarker(new MarkerOptions().position(loc).title("Your location"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 13));


            String url = "https://api.foursquare.com/v2/venues/explore" +
                    "?client_id=1MBEHFJQP1JYNKDKUHEQ31ZNILH0Q0JM5HGGSQDDUJOEGOSN" +
                    "&client_secret=QH4QJON2OX4OT5SJVD1BU0UJRP3N1UTZVVZUD5C54A5QVM5N" +
                    "&v=20180323" +
                    "&ll=" + currLat + "," + currLong +
                    "&query=hospital" +
                    "&radius=5000" +
                    "&limit=5";

            new PlaceTask().execute(url);

        } else {
            Toast.makeText(MapsActivity.this, "Failed to fetch your location", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            onLocationFetched(location);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapsActivity.this, "Failed to fetch your location", Toast.LENGTH_SHORT).show();

                        }
                    });
                }

            } else {
                Toast.makeText(this, "Provide location permission to fetch your location", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);

        } else {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    onLocationFetched(location);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapsActivity.this, "Failed to fetch your location", Toast.LENGTH_SHORT).show();

                }
            });
        }



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        fetchCurrentLocation();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listViewHospital = findViewById(R.id.listViewHospitals);

        listViewHospital.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                float lat = Float.parseFloat(hospitals.get(position).get("lat"));
                float lng = Float.parseFloat(hospitals.get(position).get("lng"));

                LatLng loc = new LatLng(lat, lng);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

            }
        });
    }
}