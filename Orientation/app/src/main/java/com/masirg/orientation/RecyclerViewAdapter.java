package com.masirg.orientation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.masirg.orientation.Domain.Track;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();

    private List<Track> tracks;
    private LayoutInflater inflater;
    private ItemClickListener mItemClickListener;
    private ItemButtonsClickListener mItemButtonsClickListener;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-YYYY HH:mm");

    public RecyclerViewAdapter(Context context,
                               List<Track> tracks,
                               @NonNull ItemButtonsClickListener buttonsClickListener,
                               @NonNull ItemClickListener itemClickListener) {
        this.tracks = tracks;
        this.inflater = LayoutInflater.from(context);
        this.mItemButtonsClickListener = buttonsClickListener;
        this.mItemClickListener = itemClickListener;
    }

    //Inflate the view from xml layout when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View rowView = inflater.inflate(R.layout.recycler_data_row, viewGroup, false);
        return new ViewHolder(rowView);
    }

    // Binds data to the view elements in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Track track = tracks.get(i);

        viewHolder.mTrackIdTextView.setText(Integer.toString(i + 1));
        viewHolder.mTrackTimeTextView.setText(String.format("%d:%02d:%02d",
                track.getTotalTime() / 3600, track.getTotalTime() / 60 , track.getTotalTime() % 60));
        viewHolder.mTrackCreatedTimeTextView.setText(formatter.format(new Date(track.getCreationTime() * 1000)));

        if (mItemButtonsClickListener == null) throw new NullPointerException("mItemButtonsClickListener is null. Please set an ItemButtonsClickListener for RecyclerViewAdapter");

        viewHolder.mViewTrackButton.setOnClickListener(v -> mItemButtonsClickListener.onViewButtonClick(track.getTrackId(), i));
        viewHolder.mEditTrackButton.setOnClickListener(v -> mItemButtonsClickListener.onEditButtonClick(track.getTrackId(), i));
        viewHolder.mDeleteTrackButton.setOnClickListener(v -> mItemButtonsClickListener.onDeleteButtonClick(track.getTrackId(), i));
    }

    @Override
    public int getItemCount() {
        return tracks.size();
    }

    //====================================== View holder class =====================================
    // Stores and recycles views as they are scrolled off the screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTrackIdTextView;
        private TextView mTrackCreatedTimeTextView;
        private TextView mTrackTimeTextView;
        private TextView mTrackDistanceTextView;
        private ImageButton mViewTrackButton;
        private ImageButton mEditTrackButton;
        private ImageButton mDeleteTrackButton;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTrackIdTextView = itemView.findViewById(R.id.trackIdTextView);
            mTrackCreatedTimeTextView = itemView.findViewById(R.id.trackCreatedTimeTextView);
            mViewTrackButton = itemView.findViewById(R.id.viewTrackButton);
            mEditTrackButton = itemView.findViewById(R.id.editTrackButton);
            mDeleteTrackButton = itemView.findViewById(R.id.deleteTrackButton);
            mTrackDistanceTextView = itemView.findViewById(R.id.trackDistance);
            mTrackTimeTextView = itemView.findViewById(R.id.trackTime);

            //addOnClickEvent handlers here
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: recycler row");
            if (mItemClickListener != null)
                mItemClickListener.onRecyclerRowItemClick(itemView, getAdapterPosition());
            else
                throw new NullPointerException("Please set an ItemClickListener for RecyclerViewAdapter");
        }
    }

    //======================= Recycler view row on click listener interface ========================

    public interface ItemClickListener {
        void onRecyclerRowItemClick(View view, int position);
    }
    public interface ItemButtonsClickListener {
        void onEditButtonClick(long trackId, int position);
        void onViewButtonClick(long trackId, int position);
        void onDeleteButtonClick(long trackId, int position);
    }
}
