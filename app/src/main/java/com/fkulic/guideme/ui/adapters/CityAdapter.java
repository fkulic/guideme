package com.fkulic.guideme.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fkulic.guideme.R;
import com.fkulic.guideme.model.City;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Filip on 26.8.2017..
 */

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ViewHolder> {

    private static List<City> mCities;
    private static OnCityClick callback;

    public interface OnCityClick {
        void onCityClick(City city);
    }

    public CityAdapter(List<City> cities, OnCityClick onCityClick) {
        mCities = cities;
        callback = onCityClick;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        City city = mCities.get(position);
        holder.setLabels(city.name, city.adminArea, city.country);
    }

    @Override
    public int getItemCount() {
        return mCities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.llCity) LinearLayout llCity;
        @BindView(R.id.tvCityName) TextView tvCityName;
        @BindView(R.id.tvCityAdminArea) TextView tvCityAdminArea;
        @BindView(R.id.tvCityCountry) TextView tvCityCountry;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setLabels(String name, String adminArea, String country) {
            tvCityName.setText(name);
            tvCityAdminArea.setText(adminArea);
            tvCityCountry.setText(country);
        }

        @OnClick(R.id.llCity)
        public void cityClick() {
            callback.onCityClick(mCities.get(getLayoutPosition()));
        }
    }

}
