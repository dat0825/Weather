package com.example.datvtd.weather;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import com.squareup.picasso.Picasso;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TodayWeatherFagment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TodayWeatherFagment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TodayWeatherFagment extends Fragment {

    public static TodayWeatherFagment instance;

    public static TodayWeatherFagment getInstance() {
        if (instance == null) {
            instance = new TodayWeatherFagment();
        }
        return instance;
    }

    public TodayWeatherFagment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TodayWeatherFagment.
     */
    // TODO: Rename and change types and number of parameters
    public static TodayWeatherFagment newInstance(String param1, String param2) {
        TodayWeatherFagment fragment = new TodayWeatherFagment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View itemView = inflater.inflate(R.layout.fragment_today_weather_fagment, container, false);
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
        getWeatherInformation();
        return itemView;
    }

    public void getWeatherInformation() {
        compositeDisposable.add((Disposable) mService.getWeatherByLatLng(
                String.valueOf(Common.current_location.getLatitude()),
                String.valueOf(Common.current_location.getLongitude()),
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
                Toast.makeText(getActivity(), ""+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        })
        );
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private ImageView img_weather;
    private TextView txtCityName, txtHumidity, txtSunrise, txtSunset, txtPressure, txtTemperature, txtDescription, txtDateTime, txtWind, txtGeoCoord;
    private LinearLayout weatherPanel;
    private ProgressBar loading;
    private CompositeDisposable compositeDisposable;
    private IOpenWeatherMap mService;
}
