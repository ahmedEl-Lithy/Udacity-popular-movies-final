package com.example.lithy.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TrailerAdapter extends BaseAdapter {

    private final Context movContext;
    private final LayoutInflater movInflater;
    private final Trailer movTrail = new Trailer();

    private List<Trailer> movObj;

    public TrailerAdapter(Context context, List<Trailer> objects) {
        movContext = context;
        movInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        movObj = objects;
    }

    public Context getContext() {
        return movContext;
    }

    public void add(Trailer object) {
        synchronized (movTrail) {
            movObj.add(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (movTrail) {
            movObj.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return movObj.size();
    }

    @Override
    public Trailer getItem(int position) {
        return movObj.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;

        if (view == null) {
            view = movInflater.inflate(R.layout.movie_trailer, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        final Trailer trailer = getItem(position);

        viewHolder = (ViewHolder) view.getTag();

        String yt_thumbnail_url = "http://img.youtube.com/vi/" + trailer.getKey() + "/0.jpg";
        Glide.with(getContext()).load(yt_thumbnail_url).into(viewHolder.imageView);

        viewHolder.nameView.setText(trailer.getName());

        return view;
    }

    public static class ViewHolder {
        public final ImageView imageView;
        public final TextView nameView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.trailer_image);
            nameView = (TextView) view.findViewById(R.id.trailer_name);
        }
    }

}
