package com.example.leon.xixiweather;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.leon.xixiweather.db.City;
import com.example.leon.xixiweather.db.County;
import com.example.leon.xixiweather.db.Province;
import com.example.leon.xixiweather.util.HttpUtil;
import com.example.leon.xixiweather.util.Utility;

import org.json.JSONException;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Leon on 2017/7/15.
 */

public class ChooseAreaFragment extends Fragment {
    public  static final  int LEVEL_PROVINCE = 0;
    public  static final int LEVEL_CITY=1;
    public static  final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String>  adapter;
    private List<String>  dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City>  cityList;
    private  List<County>  countyList;
    private Province selectedProvince;
    private City selectedCity;
    private  int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.choose_area,container,false);
        titleText =(TextView) view.findViewById(R.id.title_text);
        backButton= (Button) view.findViewById(R.id.back_button);
        listView= (ListView) view.findViewById(R.id.list_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        }
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE){
                    selectedProvince= provinceList.get(position);
                    queryCities();
                }else if(currentLevel==LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();

                }else if(currentLevel==LEVEL_COUNTY){
                    String weatherId= countyList.get(position).getWeatherId();
                    if (getActivity()instanceof  MainActivity) {


                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (getActivity()instanceof  WeatherActivity){
                        WeatherActivity activity= (WeatherActivity) getActivity();
                        activity.drawerlayout.closeDrawers();
                        activity.swipeRefesh.setRefreshing(true);
                        activity.requestWeather(weatherId);

                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();

                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();

                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
    titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList= DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province: provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
            
        }else{
            String  address="http://guolin.tech/api/china/";
            queryFromServer(address,"province");
        }
    }



    private void queryCounties() {
    titleText.setText(selectedCity.getCityNmae());
        backButton.setVisibility(View.VISIBLE);
        countyList=DataSupport.where("cityid = ?",String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyNmae());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }else{
            int  cityCode = selectedCity.getCityCode();
            int provinceCode = selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }

    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList){
                dataList.add(city.getCityNmae());

            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    private void queryFromServer(String address, final  String type) {
      showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                boolean result= false;
                if ("province".equals(type)){
                    try {
                        result= Utility.handleProvinceResponse(responseText);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else if("city".equals(type)){
                    try {
                        result=Utility.handleCityResponse(responseText,selectedProvince.getId());
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }else if("county".equals(type)){
                    try {
                        result=Utility.handleCountyResponse(responseText,selectedCity.getId());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });



    }

    private void closeProgressDialog() {
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }


    private void showProgressDialog() {
        if (progressDialog==null){
        progressDialog= new ProgressDialog(getActivity());
        progressDialog.setMessage("正在加载");
        progressDialog.setCanceledOnTouchOutside(false);

        }
        progressDialog.show();
    }


}
