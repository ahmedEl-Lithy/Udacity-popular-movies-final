package com.example.lithy.popularmovies;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


public class Movie implements Parcelable {

    private int id;
    private String title;
    private String poster;
    private String backdrop;
    private String overview;
    private int rating;
    private String date;

    public Movie() { }


    public Movie(JSONObject movie) throws JSONException {
        this.id = movie.getInt("id");
        this.title = movie.getString("original_title");
        this.poster = movie.getString("poster_path");
        this.backdrop = movie.getString("backdrop_path");
        this.overview = movie.getString("overview");
        this.rating = movie.getInt("vote_average");
        this.date = movie.getString("release_date");
    }

    public Movie(Cursor cursor) {
        this.id = cursor.getInt(MainActivityFragment.COL_MOVIE_ID);
        this.title = cursor.getString(MainActivityFragment.COL_TITLE);
        this.poster = cursor.getString(MainActivityFragment.COL_POSTER);
        this.backdrop = cursor.getString(MainActivityFragment.COL_BACKDROP);
        this.overview = cursor.getString(MainActivityFragment.COL_OVERVIEW);
        this.rating = cursor.getInt(MainActivityFragment.COL_RATING);
        this.date = cursor.getString(MainActivityFragment.COL_DATE);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getposter() {
        return poster;
    }

    public String getbackdrop() {
        return backdrop;
    }

    public String getOverview() {
        return overview;
    }

    public int getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(poster);
        dest.writeString(backdrop);
        dest.writeString(overview);
        dest.writeInt(rating);
        dest.writeString(date);
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        poster = in.readString();
        backdrop = in.readString();
        overview = in.readString();
        rating = in.readInt();
        date = in.readString();
    }
}