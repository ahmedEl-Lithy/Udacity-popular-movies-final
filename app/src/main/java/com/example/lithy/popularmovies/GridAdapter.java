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


public class GridAdapter extends BaseAdapter {

    private final Context movContext;
    private final LayoutInflater movInflater;

    private final Movie mov = new Movie();

    private List<Movie> movObj;

    public GridAdapter(Context context, List<Movie> objects) {
        movContext = context;
        movInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        movObj = objects;
    }

    public Context getContext() {
        return movContext;
    }

    public void add(Movie object) {
        synchronized (mov) {
            movObj.add(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (mov) {
            movObj.clear();
        }
        notifyDataSetChanged();
    }

    public void setData(List<Movie> data) {
        clear();
        for (Movie movie : data) {
            add(movie);
        }
    }

    @Override
    public int getCount() {
        return movObj.size();
    }

    @Override
    public Movie getItem(int position) {
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
            view = movInflater.inflate(R.layout.grid_item_movie, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        final Movie movie = getItem(position);

        String image_url = "http://image.tmdb.org/t/p/w185" + movie.getposter();

        viewHolder = (ViewHolder) view.getTag();

        Glide.with(getContext()).load(image_url).into(viewHolder.imageView);
        viewHolder.titleView.setText(movie.getTitle());

        return view;
    }

    public static class ViewHolder {
        public final ImageView imageView;
        public final TextView titleView;

        public ViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.grid_item_image);
            titleView = (TextView) view.findViewById(R.id.grid_item_title);
        }
    }
}

