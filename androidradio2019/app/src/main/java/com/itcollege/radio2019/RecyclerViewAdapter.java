package com.itcollege.radio2019;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.itcollege.radio2019.Domain.Artist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static final String TAG = RecyclerViewAdapter.class.getSimpleName();

    private List<Artist> artists;
    private LayoutInflater inflater;
    private ItemClickListener mItemClickListener;

    public RecyclerViewAdapter(Context context, List<Artist> artists) {
        this.artists = artists;
        this.inflater = LayoutInflater.from(context);
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
        viewHolder.mTextViewArtistName.setText(artists.get(i).getArtistName());
        int timesPlayed = artists.get(i).getTimesPlayed();
        viewHolder.mTextViewSongsCount.setText(Integer.toString(timesPlayed));

        SpannableStringBuilder sb = new SpannableStringBuilder();
        if (timesPlayed > 0) {
            List<Map.Entry<String, Integer>> entryList = new ArrayList<>(artists.get(i).uniqueSongs.entrySet());
            entryList.sort((e1, e2) -> Integer.compare(e1.getValue(), e2.getValue()));

            for (Map.Entry<String, Integer> entry : entryList) {
                int start = 0, end = 0;
                start = sb.length();
                sb.append("\"").append(entry.getKey()).append("\"");
                end = sb.length() + 1;
                        sb.append(" played ")
                        .append(entry.getValue().toString())
                        .append(" times\n");
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            }
        } else sb.append("No songs found, error!");

        viewHolder.mTextViewSongs.setText(sb);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public void setmItemClickListener(ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    //====================================== View holder class =====================================
    // Stores and recycles views as they are scrolled off the screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTextViewArtistName;
        private TextView mTextViewSongsCount;
        private TextView mTextViewSongs;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewArtistName = itemView.findViewById(R.id.textViewArtistName);
            mTextViewSongsCount = itemView.findViewById(R.id.textViewSongsCount);
            mTextViewSongs = itemView.findViewById(R.id.textViewSongs);
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
}
