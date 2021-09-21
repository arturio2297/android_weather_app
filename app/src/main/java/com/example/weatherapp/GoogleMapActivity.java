package com.example.weatherapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GoogleMapActivity extends AppCompatActivity {

    private SupportMapFragment supportMapFragment;
    private FusedLocationProviderClient client;
    private TextView textView;

    private final String COORDINATES = "coordinates";
    private final String LOCATION = "location";
    private String define_location;
    private String locationInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        Intent intent = getIntent();
        getSupportActionBar().hide();
        textView = findViewById(R.id.infoMarkerTV);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private void getCurrentLocation() {
        @SuppressLint("MissingPermission") Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        getAddress(getApplicationContext(), lat, lon);
                        LatLng latLng = new LatLng(lat, lon);
                        setMarkerAndAnimateCamera(googleMap, latLng, getString(R.string.current_location) + "\n" + locationInfo);
                        googleMap.setOnMapClickListener(latLng1 -> {
                            double latitude = latLng1.latitude;
                            double longitude = latLng1.longitude;
                            getAddress(getApplicationContext(), latitude, longitude);
                            setMarkerAndAnimateCamera(googleMap, latLng1, locationInfo);
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        }
    }

    private void setMarkerAndAnimateCamera(GoogleMap googleMap, LatLng latLng, String title) {
        googleMap.clear();
        MarkerOptions options = new MarkerOptions().position(latLng);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
        googleMap.addMarker(options);
        Toast.makeText(this, title, Toast.LENGTH_LONG).show();
    }

    private void getAddress(Context context, double LATITUDE, double LONGITUDE) {
        define_location = String.valueOf(LATITUDE) + ":" + String.valueOf(LONGITUDE);
        String location = "";
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                location += city == null ? "" : getString(R.string.city_name)  + " " + city + "\n";
                location += state == null ? "" : getString(R.string.province) + " " + state + "\n";
                location += country == null ? "" : getString(R.string.country) + " " + country;
            } else {
                location = getString(R.string.unknown_location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationInfo = location;
    }

    public void defineWeatherOnClickButtonMethod(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(COORDINATES, define_location);
        intent.putExtra(LOCATION, locationInfo);
        startActivity(intent);
    }

    public void defineLocationOfUserOnClick(View view) {
        getCurrentLocation();
    }
}