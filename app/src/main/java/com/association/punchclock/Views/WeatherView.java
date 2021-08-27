package com.association.punchclock.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.association.punchclock.Models.Weatherbit;
import com.association.punchclock.R;
import com.association.punchclock.Utils.ApiService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class WeatherView extends FrameLayout {
    public ProgressBar prog_location;
    public LinearLayout lay_weather;
    public TextView txt_country;
    public TextView txt_observation_time;
    public TextView txt_temperate;
    public ImageView img_weather;
    public TextView txt_weather_desc;
    public TextView txt_humidiry;
    public TextView txt_pressure;

    public WeatherView(Context context) {
        super(context);
        init();
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.layout_weather, this);

        prog_location = findViewById(R.id.prog_location);
        lay_weather = findViewById(R.id.lay_weather);
        txt_country = findViewById(R.id.txt_country);
        txt_observation_time = findViewById(R.id.txt_observation_time);
        txt_temperate = findViewById(R.id.txt_temperate);
        img_weather = findViewById(R.id.img_weather);
        txt_weather_desc = findViewById(R.id.txt_weather_desc);
        txt_humidiry = findViewById(R.id.txt_humidiry);
        txt_pressure = findViewById(R.id.txt_pressure);
        Timer myTimer = new Timer();
        //Set the schedule function and rate
        myTimer.scheduleAtFixedRate(new TimerTask() {
                                        @Override
                                        public void run() {
                                            getWeather();
                                        }
                                    },
                0,
                30 * 60 * 1000);
    }

    public void getWeather() {
        ApiService.getWeather(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject response) {
                Weatherbit weatherbit = new Weatherbit();
                try {
                    JSONObject data = response.getJSONArray("data").getJSONObject(0);
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data.toString());
                    Gson gson = new Gson();
                    weatherbit = gson.fromJson(mJson, Weatherbit.class);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                updateUi(weatherbit);
            }

            @Override
            public void onError(ANError anError) {
                Weatherbit weatherbit = new Weatherbit();
                updateUi(weatherbit);
            }
        });
    }

    public void updateUi(Weatherbit weatherbit) {
        prog_location.setVisibility(View.GONE);
        lay_weather.setVisibility(VISIBLE);
        String country = weatherbit.city_name + ", " + weatherbit.country_code;
        String updated_date = "Last update: " + weatherbit.ob_time;
        String temperate = weatherbit.temp + " °C";
        String desc = weatherbit.weather.description;
        String humidity = "Humidity: " + weatherbit.rh + "%";
        String pres = "Pressure: " + weatherbit.Pressure() + "kPa";

        txt_country.setText(country);
        txt_observation_time.setText(updated_date);
        txt_temperate.setText(temperate);
        txt_weather_desc.setText(desc);
        txt_humidiry.setText(humidity);
        txt_pressure.setText(pres);
        ApiService.getBitmap(weatherbit.weatherImage(), new BitmapRequestListener() {
            @Override
            public void onResponse(Bitmap response) {
                img_weather.setImageBitmap(response);
            }

            @Override
            public void onError(ANError anError) {
                img_weather.setVisibility(View.GONE);
            }
        });
    }

}
