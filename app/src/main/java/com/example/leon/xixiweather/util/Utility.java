package com.example.leon.xixiweather.util;

import android.text.TextUtils;

import com.example.leon.xixiweather.db.City;
import com.example.leon.xixiweather.db.County;
import com.example.leon.xixiweather.db.Province;
import com.example.leon.xixiweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Leon on 2017/7/15.
 */

public class Utility {
    public  static    boolean handleProvinceResponse(String response) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            try {
            JSONArray allProvinces = new JSONArray(response);
            for (int i = 0; i <allProvinces.length() ; i++) {
                JSONObject provinceObject = allProvinces.getJSONObject(i);
                Province province = new Province();
                province.setProvinceName(provinceObject.getString("name"));
                province.setProvinceCode(provinceObject.getInt("id"));
                province.save();


            }  return true;
        }catch (JSONException e){
            e.printStackTrace();
        }
        }
    return  false;
    }
    public  static   boolean handleCityResponse(String response, int provinId) throws JSONException {
        if (!TextUtils.isEmpty(response)){
            try {
            JSONArray allCities = new JSONArray(response);
            for (int i = 0; i <allCities.length() ; i++) {
                JSONObject cityObject= allCities.getJSONObject(i);
                City city = new City();
                city.setCityNmae(cityObject.getString("name"));
                city.setCityCode(cityObject.getInt("id"));
                city.setProvinceId(provinId);
                city.save();


            }return  true;
        }catch (JSONException e){
            e.printStackTrace();
        }
        }
        return  false;
    }
    public  static    boolean handleCountyResponse(String response,int cityId) throws JSONException {
        if (!TextUtils.isEmpty(response)){try {


            JSONArray allCounties = new JSONArray(response);
            for (int i = 0; i < allCounties.length(); i++) {
                JSONObject countyObject = allCounties.getJSONObject(i);
                County county = new County();
                county.setCountyNmae(countyObject.getString("name"));
                county.setCityId(cityId);
                county.save();


            }
            return true;
        }catch (JSONException e){
            e.printStackTrace();
        }
        }
        return  false;
    }
public  static Weather handleWeatherResponse(String response){

    try {
        JSONObject jsonObject=new JSONObject(response);
        JSONArray jsonArray =jsonObject.getJSONArray("HeWeather");
        String weatherContent =jsonArray.getJSONObject(0).toString();
        return  new Gson().fromJson(weatherContent ,Weather.class);
    } catch (JSONException e) {
        e.printStackTrace();
    }
    return null;
}
}
