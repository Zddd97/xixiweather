package com.example.leon.xixiweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Leon on 2017/7/16.
 */

public class Weather {
    public  String status;
    public  Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;
    @SerializedName("daily_forecast")
    public List<Forecast>  forecastList;
}
