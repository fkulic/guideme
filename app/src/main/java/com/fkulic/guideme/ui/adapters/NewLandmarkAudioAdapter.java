package com.fkulic.guideme.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.fkulic.guideme.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Filip on 8.9.2017..
 */

public class NewLandmarkAudioAdapter extends RecyclerView.Adapter<NewLandmarkAudioAdapter.ViewHolder> {

    private List<String> mAudioNames;
    private static OnRemoveAudioFile callback;


    public interface OnRemoveAudioFile {
        void onRemoveAudioFile(String audioName);
    }

    public NewLandmarkAudioAdapter(List<String> audioNames, OnRemoveAudioFile onRemoveAudioFile) {
        this.mAudioNames = audioNames;
        this.callback = onRemoveAudioFile;
    }

    public void addAudio(String name) {
        mAudioNames.add(name);
        notifyDataSetChanged();
    }

    public void removeAudio(String name) {
        mAudioNames.remove(name);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_new_landmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvAudioNameNewLandmark.setText(mAudioNames.get(position));
    }

    @Override
    public int getItemCount() {
        return mAudioNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvAudioNameNewLandmark) TextView tvAudioNameNewLandmark;
        @BindView(R.id.ibRemoveAudio) ImageButton ibRemoveAudio;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.ibRemoveAudio)
        public void onClickRemove() {
            callback.onRemoveAudioFile(tvAudioNameNewLandmark.getText().toString());
        }
    }
}
