package com.example.datvtd.weather;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.datvtd.weather.Adapter.WeatherForecastAdapter;
import com.example.datvtd.weather.Common.Common;
import com.example.datvtd.weather.Model.WeatherForecastResult;
import com.example.datvtd.weather.Retrofit.IOpenWeatherMap;
import com.example.datvtd.weather.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

    public static ForecastFragment instance;
    public CompositeDisposable compositeDisposable;
    public IOpenWeatherMap mService;
    public TextView txtCityName, txtGeoCoord;
    public RecyclerView recyclerForecast;

    public static ForecastFragment getInstance() {
        if (instance == null) {
            instance = new ForecastFragment();
        }

        return instance;
    }

    public ForecastFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_forecast, container, false);
        txtCityName = itemView.findViewById(R.id.txt_city_name);
        txtGeoCoord = itemView.findViewById(R.id.txt_geo_coord);

        recyclerForecast = itemView.findViewById(R.id.recycler_forecast);
        recyclerForecast.setHasFixedSize(true);
        recyclerForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        getForecastWeatherInfomation();
        return itemView;
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    public void getForecastWeatherInfomation() {
        compositeDisposable.add(mService.getForecastWeatherByLatLng(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherForecastResult>() {
                    @Override
                    public void accept(WeatherForecastResult weatherForecastResult) throws Exception {
                        displayForecastWeather(weatherForecastResult);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d("VLXXXX",""+throwable.getMessage());
                    }
                })
        );
    }

    private void displayForecastWeather(WeatherForecastResult weatherForecastResult) {
//        //load image
//        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
//                .append(weatherResult.getWeather().get(0).getIcon())
//                .append(".png").toString()).into(img_weather);
        txtCityName.setText(new StringBuilder(weatherForecastResult.city.name));
        txtGeoCoord.setText(new StringBuilder(weatherForecastResult.city.coord.toString()));
        WeatherForecastAdapter adapter = new WeatherForecastAdapter(getContext(), weatherForecastResult);
        recyclerForecast.setAdapter(adapter);
    }

}
