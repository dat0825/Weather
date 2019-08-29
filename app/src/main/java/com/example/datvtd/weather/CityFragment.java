package com.example.datvtd.weather;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datvtd.weather.Common.Common;
import com.example.datvtd.weather.Model.WeatherResult;
import com.example.datvtd.weather.Retrofit.IOpenWeatherMap;
import com.example.datvtd.weather.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.label305.asynctask.SimpleAsyncTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class CityFragment extends Fragment {

    public static CityFragment instance;

    public static CityFragment getInstance() {
        if (instance == null) {
            instance = new CityFragment();
        }
        return instance;
    }


    public CityFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_city, container, false);
        img_weather = itemView.findViewById(R.id.img_weather);
        txtCityName = itemView.findViewById(R.id.txt_city_name);
        txtDateTime = itemView.findViewById(R.id.txt_date_time);
        txtDescription = itemView.findViewById(R.id.txt_description);
        txtGeoCoord = itemView.findViewById(R.id.txt_geo_coord);
        txtHumidity = itemView.findViewById(R.id.txt_humidity);
        txtPressure = itemView.findViewById(R.id.txt_pressure);
        txtSunrise = itemView.findViewById(R.id.txt_sunrise);
        txtSunset = itemView.findViewById(R.id.txt_sunset);
        txtTemperature = itemView.findViewById(R.id.txt_temperature);

        weatherPanel = itemView.findViewById(R.id.weather_panel);
        loading = itemView.findViewById(R.id.loading);

        searchBar = itemView.findViewById(R.id.searchBar);
        searchBar.setEnabled(false);
        new LoadCities().execute(); //AsyncTask class to load cities
        return itemView;
    }

    public class LoadCities extends SimpleAsyncTask<List<String>> {

        @Override
        protected List<String> doInBackgroundSimple() {
            listCities = new ArrayList<>();
            try {
                StringBuilder builder = new StringBuilder();
                InputStream inputStream = getResources().openRawResource(R.raw.city_list);
                GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);

                InputStreamReader reader = new InputStreamReader(gzipInputStream);
                BufferedReader in = new BufferedReader(reader);
                String readed;
                while ((readed = in.readLine()) != null) {
                    builder.append(readed);
                }
                listCities = new Gson().fromJson(builder.toString(), new TypeToken<List<String>>() {
                }.getType());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return listCities;
        }

        @Override
        protected void onSuccess(final List<String> listCity) {
            super.onSuccess(listCity);
            searchBar.setEnabled(true);
            searchBar.addTextChangeListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    List<String> suggestCity = new ArrayList<>();
                    for (String search : listCity) {
                        if (search.toLowerCase().contains(searchBar.getText().toLowerCase())) {
                            suggestCity.add(search);
                        }
                    }
                    searchBar.setLastSuggestions(suggestCity);

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
                @Override
                public void onSearchStateChanged(boolean enabled) {
                }

                @Override
                public void onSearchConfirmed(CharSequence text) {
                    getWeatherInformation(text.toString());
                    searchBar.setLastSuggestions(listCity);
                }

                @Override
                public void onButtonClicked(int buttonCode) {

                }
            });
            searchBar.setLastSuggestions(listCity);
            loading.setVisibility(View.GONE);
        }
    }

    public void getWeatherInformation(String cityName) {
        compositeDisposable.add((Disposable) mService.getWeatherByCityName(
                cityName,
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        //load image
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                                .append(weatherResult.getWeather().get(0).getIcon())
                                .append(".png").toString()).into(img_weather);

                        //load information
                        txtCityName.setText(weatherResult.getName());
                        txtDescription.setText(new StringBuilder("Weather in ")
                                .append(weatherResult.getName()).toString());
                        txtTemperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain()
                                .getTemp())).append("°C").toString());
                        txtDateTime.setText(Common.convertUnixToDate(weatherResult.getDt()));
                        txtPressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure())).append(" hpa").toString());
                        txtHumidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append(" %").toString());
                        txtSunrise.setText(Common.convertUnixToHour(weatherResult.getSys().getSunrise()));
                        txtSunset.setText(Common.convertUnixToHour(weatherResult.getSys().getSunset()));
                        txtGeoCoord.setText(new StringBuilder(weatherResult.getCoord().toString()).toString());

                        //display panel
                        weatherPanel.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(getActivity(), "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );
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

    private List<String> listCities;
    private MaterialSearchBar searchBar;
    private ImageView img_weather;
    private TextView txtCityName, txtHumidity, txtSunrise, txtSunset, txtPressure, txtTemperature, txtDescription, txtDateTime, txtWind, txtGeoCoord;
    private LinearLayout weatherPanel;
    private ProgressBar loading;
    private CompositeDisposable compositeDisposable;
    private IOpenWeatherMap mService;
}
