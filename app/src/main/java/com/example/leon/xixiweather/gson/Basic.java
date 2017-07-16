package com.example.leon.xixiweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Leon on 2017/7/16.
 */

public class Basic {
    @SerializedName("city")
    public String cityNmae;
    @SerializedName("id")
    public  String weatherId;
    public  Update update;
    public  class Update{
        @SerializedName("loc")
        public  String  updateTime;

    }
}
