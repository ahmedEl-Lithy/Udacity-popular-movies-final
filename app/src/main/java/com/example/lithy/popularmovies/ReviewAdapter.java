package com.example.lithy.popularmovies;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class ReviewAdapter extends BaseAdapter {

    private final Context movContext;
    private final LayoutInflater movInflater;
    private final Review movReview = new Review();

    private List<Review> movObj;

    public ReviewAdapter(Context context, List<Review> objects) {
        movContext = context;
        movInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        movObj = objects;
    }

    public Context getContext() {
        return movContext;
    }

    public void add(Review object) {
        synchronized (movReview) {
            movObj.add(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (movReview) {
            movObj.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return movObj.size();
    }

    @Override
    public Review getItem(int position) {
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
            view = movInflater.inflate(R.layout.movie_review, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        final Review review = getItem(position);

        viewHolder = (ViewHolder) view.getTag();

        viewHolder.authorView.setText(review.getAuthor());
        viewHolder.contentView.setText(Html.fromHtml(review.getContent()));

        return view;
    }

    public static class ViewHolder {
        public final TextView authorView;
        public final TextView contentView;

        public ViewHolder(View view) {
            authorView = (TextView) view.findViewById(R.id.review_author);
            contentView = (TextView) view.findViewById(R.id.review_content);
        }
    }

}

