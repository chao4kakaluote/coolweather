package com.example.administrator.coolweather;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.administrator.coolweather.gson.Forecast;
import com.example.administrator.coolweather.gson.Weather;
import com.example.administrator.coolweather.service.AutoUpdateService;
import com.example.administrator.coolweather.util.HttpUtil;
import com.example.administrator.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastlayout;
    private TextView aqiText;
    private TextView pm25text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;

    private ImageView bingPicImage;
    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private Button navButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("weatherActivity","onCreate");
        CollaspingToolbar();
        setContentView(R.layout.activity_weather);
        Log.d("weatherAcitivy","setContentView");
        weatherLayout=(ScrollView)findViewById(R.id.weather_layout);
        titleCity=(TextView)findViewById(R.id.title_city);
        titleUpdateTime=(TextView)findViewById(R.id.title_update_time);
        degreeText=(TextView)findViewById(R.id.degree_txt);
        weatherInfoText=(TextView)findViewById(R.id.weather_info_text);
        forecastlayout=(LinearLayout)findViewById(R.id.forecast_layout);
        aqiText=(TextView)findViewById(R.id.api_text);
        pm25text=(TextView)findViewById(R.id.pm25_txt);
        comfortText=(TextView)findViewById(R.id.comfort_text);
        carWashText=(TextView)findViewById(R.id.car_wash_text);
        sportText=(TextView)findViewById(R.id.support_text);
        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        navButton=(Button)findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        Log.d("weatherActivity","setLayout");

        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prefs.getString("weather",null);


        bingPicImage=(ImageView)findViewById(R.id.bing_pic_img);
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null)
            Glide.with(this).load(bingPic).into(bingPicImage);
        else
            loadBingPic();
        Log.d("WeatherOnCreate","weatheroncreate");

        String weatherId="";
        if(weatherString!=null)
        {
            Weather weather= Utility.handleWeatherResponse(weatherString);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
            refreshAuto(weatherId);
        }
        else
        {
            Log.d("getWeatherId","getWeatherId");
            weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            refreshAuto(weatherId);
        }
    }
    public void requestWeather(final String weatherId)
    {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=2187af6778354af9a6cbdd3f0e0ce1db";
        Log.d("weatherId",weatherId);
        Log.d("requestWeather","requestWeather");
        HttpUtil.sendOkHttpRequest(weatherUrl,new Callback()
        {
            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                Log.d("response","OnResponse");
                 final String responseText=response.body().string();
                Log.d("responseText",responseText);
                 final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else
                        {
                            Log.d("responseFail","responseFail");
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        Log.d("requestFinished","finished");
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("failure","failed");
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }


        });
    }

    private void showWeatherInfo(Weather weather)
    {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String status=weather.status;
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastlayout.removeAllViews();


        for(Forecast forecast:weather.forecastList)
        {
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastlayout,false);
            TextView dateText=(TextView)view.findViewById(R.id.date_text);
            TextView infoText=(TextView)view.findViewById(R.id.info_txt);
            TextView maxText=(TextView)view.findViewById(R.id.max_text);
            TextView minText=(TextView)view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.temperature.max);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastlayout.addView(view);
        }
        if(weather.aqi!=null)
        {
            aqiText.setText(weather.aqi.city.aqi);
            pm25text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度 "+weather.suggestion.comfort.info;
        String carWash="洗车指数"+weather.suggestion.carWash.info;
        String sport="运动建议"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    private void loadBingPic()
    {
          String requestBingPic="http://guolin.tech/api/bing_pic";
          HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                  e.printStackTrace();
              }
              @Override
              public void onResponse(Call call, Response response) throws IOException {
                  final String bingPic=response.body().string();
                  SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                  editor.putString("bing_pic",bingPic);
                  editor.apply();
                  runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                          Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImage);
                      }
                  });
              }
          });
    }

    private void CollaspingToolbar()
    {
        if(Build.VERSION.SDK_INT>=21)
        {
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
    public void refreshAuto(final String weatherId)
    {
        Log.d("refreshAuto","refreshAuto");
         swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
         {
             @Override
             public void onRefresh() {
                 Log.d("OnRefresh","OnRefresh");
                 requestWeather(weatherId);
             }
         });
    }

    @Override
    protected void onDestroy() {
        Intent intent=new Intent(this,AutoUpdateService.class);
        stopService(intent);
        super.onDestroy();
    }
}
