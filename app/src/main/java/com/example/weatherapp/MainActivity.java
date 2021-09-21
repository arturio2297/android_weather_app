package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private Button button1;
    private Button button2;
    private EditText editText;
    private TextView infoWeatherTextView;
    private TextView infoWindTextView;
    private ImageView imageViewArrow;
    private ImageView imageViewWeatherIcon;

    private String api_key;
    private final String COORDINATES = "coordinates";
    private final String LOCATION = "location";

    private float InitialWD = 0;
    private float DuringWD = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        api_key = getString(R.string.api_key);
        editText = findViewById(R.id.cityEditText);
        button1 = findViewById(R.id.viewWeatherButton);
        button2 = findViewById(R.id.showWeatherByLocationButton);
        infoWeatherTextView = findViewById(R.id.infoWeatherTextView);
        infoWindTextView = findViewById(R.id.textViewWindDescription);
        imageViewArrow = findViewById(R.id.imageViewArrowWind);
        imageViewWeatherIcon = findViewById(R.id.imageViewWeather);
        getIntentFromMapActivities();
        button1.setOnClickListener(view -> getContent(generateReferenceByCityName()));
        button2.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), GoogleMapActivity.class)));
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                hideKeyboard(v);
            }
        });
    }


    private String generateReferenceByCityName() {
        String sampleUrl = "https://api.openweathermap.org/data/2.5/weather?q={KEY}&units=metric&lang={lang}&appid=" + api_key;
        String urlWithLang = sampleUrl.replace("{lang}",getString(R.string.language));
        String ref = urlWithLang.replace("{KEY}", editText.getText().toString().trim());
        return ref;
    }

    private String generateReferenceByLocation(String lat, String lon) {
        String sampleUrl = "https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&units=metric&lang={lang}&appid=" + api_key;
        String urlWithLang = sampleUrl.replace("{lang}",getString(R.string.language));
        String ref = urlWithLang.replace("{lat}", lat).replace("{lon}", lon);
        return ref;
    }

    private String generateReferenceForIcon(String iconID) {
        String sample = "https://openweathermap.org/img/wn/ID@4x.png";
        String ref = sample.replace("ID",iconID);
        return ref;
    }

    private void getContent(String reference) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, reference, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    setInfoOfWeather(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.i("LOG", error.toString());
                    Toast.makeText(getApplicationContext(), getString(R.string.incorrectInput), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
        }

    private void setIcon(String iconID) {
        DownloadTaskBitMap downloadTaskBitMap = new DownloadTaskBitMap();
        try {
            Bitmap icon = downloadTaskBitMap.execute(generateReferenceForIcon(iconID)).get();
            imageViewWeatherIcon.setImageBitmap(icon);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setInfoOfWeather(JSONObject jsonObject) {
        String[] info = new String[2];
        String iconID = "";
        try {
            //weather array {0 {description, icon}}
            JSONArray jsonArrayWeather = jsonObject.getJSONArray("weather");
            JSONObject jsonWeather = jsonArrayWeather.getJSONObject(0);
            String descriptionOfWeather = jsonWeather.getString("description");
            iconID = jsonWeather.getString("icon");
            info[0] = getString(R.string.descriptionOfWeather) + " " + descriptionOfWeather + "\n" + "\n";
            //main {temp, feels like temp, temp min, temp max, pressure, humidity}
            JSONObject jsonMain = jsonObject.getJSONObject("main");
            String temp = jsonMain.getString("temp");
            String feelsLikeTemp = jsonMain.getString("feels_like");
            String temp_min = jsonMain.getString("temp_min");
            String temp_max = jsonMain.getString("temp_max");
            String pressure = jsonMain.getString("pressure");
            String humidity = jsonMain.getString("humidity");
            info[0] += getString(R.string.temp) + " " + temp + " °C" + "\n" + "\n"
                    + getString(R.string.fellsLikeTemp) + " " + feelsLikeTemp + " °C" + "\n" + "\n"
                    + getString(R.string.min_temp) + " " + temp_min + " °C" + "\n" + "\n"
                    + getString(R.string.max_temp) + " " + temp_max + " °C" +  "\n" + "\n"
                    + getString(R.string.humidity) + " " + humidity + " %" + "\n" + "\n"
                    + getString(R.string.pressure) + " " + pressure + " " + getString(R.string.pascals);
            Log.i("LOG", temp + " " + feelsLikeTemp + " " + temp_min + " " + temp_max + " " + pressure + " " + humidity);
            //wind {speed, degree}
            JSONObject jsonWind = jsonObject.getJSONObject("wind");
            String speedOfWind = jsonWind.getString("speed");
            DuringWD = Float.parseFloat(jsonWind.getString("deg"));
            info[1] = getString(R.string.speedWind) + " " + speedOfWind + " " + getString(R.string.speed);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        infoWeatherTextView.setText(info[0]);
        infoWindTextView.setText(info[1]);
        setIcon(iconID);
        startAnimation();
        imageViewArrow.setVisibility(View.VISIBLE);
    }


    private void startAnimation() {
        Animation animation = new RotateAnimation(InitialWD, DuringWD, imageViewArrow.getPivotX(), imageViewArrow.getPivotY());
        animation.setDuration(1000);
        animation.setFillAfter(true);
        imageViewArrow.startAnimation(animation);
        InitialWD = DuringWD;
    }

    private void getIntentFromMapActivities() {
        Intent intent = getIntent();
        String coordinates = intent.getStringExtra(COORDINATES);
        if (coordinates != null) {
            String[] cArray = coordinates.split(":");
            getContent(generateReferenceByLocation(cArray[0],cArray[1]));
            String locationInfo = intent.getStringExtra(LOCATION);
            editText.setHint(locationInfo);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static class DownloadTaskBitMap extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            URL url = null;
            HttpURLConnection huc = null;
            try {

                url = new URL(strings[0]);
                huc = (HttpURLConnection) url.openConnection();
                InputStream is = huc.getInputStream();
                return BitmapFactory.decodeStream(is);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}