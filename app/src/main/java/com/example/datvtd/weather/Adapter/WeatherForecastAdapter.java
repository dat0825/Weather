package com.example.datvtd.weather.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.datvtd.weather.Common.Common;
import com.example.datvtd.weather.Model.WeatherForecastResult;
import com.example.datvtd.weather.R;
import com.squareup.picasso.Picasso;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.MyViewHolder> {

    public Context context;
    public WeatherForecastResult weatherForecastResult;

    public WeatherForecastAdapter(Context context, WeatherForecastResult weatherForecastResult) {
        this.context = context;
        this.weatherForecastResult = weatherForecastResult;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_weather_forecast, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        //load icon
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.list.get(position).weather.get(0).getIcon())
                .append(".png").toString()).into(holder.img_weather);
        holder.txtDate.setText(new StringBuilder(Common.convertUnixToDate(weatherForecastResult.list.get(position).dt)));

        holder.txtDescription.setText(new StringBuilder(weatherForecastResult.list.get(position)
                .weather.get(0).getDescription()));
        holder.txtTemperature.setText(new StringBuilder(String.valueOf(weatherForecastResult.list.get(position)
                .main.getTemp())).append("°C"));
    }

    @Override
    public int getItemCount() {
        return weatherForecastResult.list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public MyViewHolder(View itemView) {
            super(itemView);
            img_weather = itemView.findViewById(R.id.img_weather);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtDescription = itemView.findViewById(R.id.txt_description);
            txtTemperature = itemView.findViewById(R.id.txt_temperature);

        }

        private TextView txtDate, txtDescription, txtTemperature;
        private ImageView img_weather;
    }
}
