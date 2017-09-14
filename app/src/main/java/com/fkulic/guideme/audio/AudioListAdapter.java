package com.fkulic.guideme.audio;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fkulic.guideme.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Filip on 31.8.2017..
 */

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.ViewHolder> {
    private static final String TAG = "AudioListAdapter";

    private List<String> mAudioNames;
    private static OnPlayAudioFile callback;

    public interface OnPlayAudioFile {
        void onPlayAudioFile(String audioName);
    }

    public AudioListAdapter(List<String> audioNames, OnPlayAudioFile onPlayAudioFile) {
        this.mAudioNames = audioNames;
        this.callback = onPlayAudioFile;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvAudioName.setText(mAudioNames.get(position));
    }

    @Override
    public int getItemCount() {
        return mAudioNames.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.llAudioFile) LinearLayout llAudioFile;
        @BindView(R.id.tvAudioName) TextView tvAudioName;
        @BindView(R.id.ivMusicNote) ImageView ivMusicNote;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.llAudioFile)
        public void onAudioClick() {
            callback.onPlayAudioFile(tvAudioName.getText().toString());
            ivMusicNote.setSelected(true);
        }

    }
}
