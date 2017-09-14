package com.fkulic.guideme.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fkulic.guideme.R;
import com.fkulic.guideme.model.Landmark;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Filip on 26.8.2017..
 */

public class LandmarkAdapter extends RecyclerView.Adapter<LandmarkAdapter.ViewHolder> {
    private static List<Landmark> mLandmarks;
    private static LandmarkOnClickListener mLandmarkOnClickListener;


    public interface LandmarkOnClickListener {
        void landmarkOnClickListener(Landmark landmark);
    }

    public LandmarkAdapter(LandmarkOnClickListener landmarkOnClickListener) {
        this.mLandmarkOnClickListener = landmarkOnClickListener;
        mLandmarks = new ArrayList<>();
    }

    @Override
    public LandmarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View landmarkView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_landmark, parent, false);
        return new ViewHolder(landmarkView);
    }

    @Override
    public void onBindViewHolder(LandmarkAdapter.ViewHolder holder, int position) {
        Landmark landmark = mLandmarks.get(position);
        holder.tvLandmarkName.setText(landmark.name);
        Picasso.with(holder.ivLandmarkPhoto.getContext())
                .load(landmark.imgUrl)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .resize(1080, 600)
                .onlyScaleDown()
                .centerCrop()
                .into(holder.ivLandmarkPhoto);
    }

    @Override
    public int getItemCount() {
        return this.mLandmarks.size();
    }

    public void loadLandmarks(List<Landmark> landmarks) {
        this.mLandmarks = landmarks;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivLandmarkPhoto) ImageView ivLandmarkPhoto;
        @BindView(R.id.tvLandmarkName) TextView tvLandmarkName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick({R.id.ivLandmarkPhoto, R.id.tvLandmarkName})
        public void landmarkClick() {
            Landmark landmark = mLandmarks.get(getLayoutPosition());
            mLandmarkOnClickListener.landmarkOnClickListener(landmark);
        }
    }

}
