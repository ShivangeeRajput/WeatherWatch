package com.example.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
//declaring all the variables
    private RelativeLayout RLHome;
    private ProgressBar ivLoading;
    private TextView tvCityName,tvTemp,tvCondition;
    private RecyclerView rvWeather;
    private TextInputEditText edtCity;
    private ImageView ivBack,ivSearch,ivIconImg;

    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    // location permission
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting flags for full screen of the app
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        //initializing all the variable
        RLHome=findViewById(R.id.RLHome);
        ivLoading=findViewById(R.id.ivLoading);
        tvCityName=findViewById(R.id.tvCityName);
        tvTemp=findViewById(R.id.tvTemp);
        tvCondition =findViewById(R.id.tvCondition);
        rvWeather=findViewById(R.id.rvWeather);
        edtCity=findViewById(R.id.edtCity);
        ivBack=findViewById(R.id.ivBack);
        ivSearch=findViewById(R.id.ivSearch);
        ivIconImg=findViewById(R.id.ivIconImg);
        weatherRVModelArrayList=new ArrayList<>();
        weatherRVAdapter=new WeatherRVAdapter(this,weatherRVModelArrayList);

        //granting permission
        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName=getCityName(location.getLongitude(),location.getLatitude());

        getWeatherInfo(cityName);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city=edtCity.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this,"Please enter city name",Toast.LENGTH_SHORT).show();
                }else{
                    tvCityName.setText(cityName);
                    getWeatherInfo(city);
                }

            }
        });





    }

    @Override
    //Granting permission
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this,"Please provide the permission",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName="Not Found";
        Geocoder gcd= new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses=gcd.getFromLocation(latitude,longitude,10);

            for(Address adr:addresses) {
            if (adr != null){
                String city=adr.getLocality();
                if(city!=null && !city.equals("")){
                    cityName=city;
                }
                else {
                    Log.d("TAG","CITY NOT FOUND");
                    Toast.makeText(this,"User city not found",Toast.LENGTH_SHORT).show();
                }
            }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;


    }
//API URL
    private void getWeatherInfo(String cityName){
        String url="http://api.weatherapi.com/v1/forecast.json?key=0b5c8de45c2d4c11843123634233005&q="+ cityName +"&days=1&aqi=no&alerts=no";
        tvCityName.setText(cityName);
        RequestQueue requestQueue=Volley.newRequestQueue(MainActivity.this);

        //json object request
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ivLoading.setVisibility(View.GONE);
                RLHome.setVisibility(View.VISIBLE);
                weatherRVModelArrayList.clear();

                try {
                    String temperature= response.getJSONObject("current").getString("temp_c");
                    tvTemp.setText(temperature+ "°c");
                    int isDay= response.getJSONObject("current").getInt("is_day");
                    String condition=response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon=response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(ivIconImg);
                    tvCondition.setText(condition);
                    if (isDay==1){
                        //morning
                        Picasso.get().load("https://t4.ftcdn.net/jpg/02/28/22/21/360_F_228222127_b7fAlMfQNKfyeKvz2BVEMHRAnK1y4FmV.jpg").into(ivBack);
                    }else{
                        //night
                        Picasso.get().load("https://r2.community.samsung.com/t5/image/serverpage/image-id/1804852i6C7FB2B2EFA7088B/image-size/large?v=v2&px=999").into(ivBack);
                    }

                    //creating forecast json object
                    JSONObject forecastObj=response.getJSONObject("forecast");
                    JSONObject forecast0=forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray=forecast0.getJSONArray("hour");

                    for (int i=0;i<hourArray.length(); i++){
                        JSONObject hourObj=hourArray.getJSONObject(i);
                        String time=hourObj.getString("time");
                        String temper=hourObj.getString("temp_c");
                        String img=hourObj.getJSONObject("condition").getString("icon");
                        String wind=hourObj.getString("wind_kph");
                        weatherRVModelArrayList.add(new WeatherRVModel(time,temper,img,wind));
                    }
                    weatherRVAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this,"Please enter valid city name",Toast.LENGTH_SHORT).show();

            }
        });

       requestQueue.add(jsonObjectRequest);
    }
}