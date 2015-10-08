package com.example.lithy.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private GridView movGrid;

    private GridAdapter movGridAdapter;

    private static final String SORT_SETTING = "sort_setting";
    private static final String POPULARITY = "popularity.desc";
    private static final String RATING = "vote_average.desc";
    private static final String FAVORITE = "favorite";
    private static final String MOVIES_KEY = "movies";

    private String movSort = POPULARITY;

    private ArrayList<Movie> movies = null;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER,
            MovieContract.MovieEntry.COLUMN_BACKDROP,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_DATE
    };

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_TITLE = 2;
    public static final int COL_POSTER = 3;
    public static final int COL_BACKDROP = 4;
    public static final int COL_OVERVIEW = 5;
    public static final int COL_RATING = 6;
    public static final int COL_DATE = 7;

    public MainActivityFragment() { }
    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem sort_by_popularity = menu.findItem(R.id.sort_by_popularity);
        MenuItem sort_by_rating = menu.findItem(R.id.sort_by_rating);
        MenuItem sort_by_favorite = menu.findItem(R.id.sort_by_favorite);

        if (movSort.contentEquals(POPULARITY)) {
            sort_by_popularity.setChecked(true);
        } else if (movSort.contentEquals(RATING)) {
                sort_by_rating.setChecked(true);
        } else if (movSort.contentEquals(FAVORITE)) {
                sort_by_favorite.setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sort_by_popularity:
                item.setChecked(false);
                movSort = POPULARITY;
                updateMovies(movSort);
                return true;
            case R.id.sort_by_rating:
                item.setChecked(false);
                movSort = RATING;
                updateMovies(movSort);
                return true;
            case R.id.sort_by_favorite:
                item.setChecked(false);
                movSort = FAVORITE;
                updateMovies(movSort);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movGrid = (GridView) view.findViewById(R.id.gridview);

        movGridAdapter = new GridAdapter(getActivity(), new ArrayList<Movie>());

        movGrid.setAdapter(movGridAdapter);

        movGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = movGridAdapter.getItem(position);
                ((Callback) getActivity()).onItemSelected(movie);
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SORT_SETTING)) {
                movSort = savedInstanceState.getString(SORT_SETTING);
            }

            if (savedInstanceState.containsKey(MOVIES_KEY)) {
                movies = savedInstanceState.getParcelableArrayList(MOVIES_KEY);
                movGridAdapter.setData(movies);
            } else {
                updateMovies(movSort);
            }
        } else {
            updateMovies(movSort);
        }

        return view;
    }

    private void updateMovies(String sort_by) {
        if (!sort_by.contentEquals(FAVORITE)) {
            new FetchMoviesTask().execute(sort_by);
        } else {
            new FetchFavoriteMoviesTask(getActivity()).execute();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (!movSort.contentEquals(POPULARITY)) {
            outState.putString(SORT_SETTING, movSort);
        }
        if (movies != null) {
            outState.putParcelableArrayList(MOVIES_KEY, movies);
        }
        super.onSaveInstanceState(outState);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private List<Movie> getMoviesDataFromJson(String jsonStr) throws JSONException {
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray("results");

            List<Movie> results = new ArrayList<>();

            for(int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                Movie movieModel = new Movie(movie);
                results.add(movieModel);
            }
            return results;
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

            if (params.length == 0) { return null; }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_BY_PARAM = "sort_by";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, params[0])
                        .appendQueryParameter(API_KEY_PARAM, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                if (movGridAdapter != null) {
                    movGridAdapter.setData(movies);
                }
                movies = new ArrayList<>();
                movies.addAll(movies);
            }
        }
    }

    public class FetchFavoriteMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

        private Context movContext;

        public FetchFavoriteMoviesTask(Context context) {
            movContext = context;
        }

        private List<Movie> getFavoriteMoviesDataFromCursor(Cursor cursor) {
            List<Movie> results = new ArrayList<>();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Movie movie = new Movie(cursor);
                    results.add(movie);
                } while (cursor.moveToNext());
                cursor.close();
            }
            return results;
        }

        @Override
        protected List<Movie> doInBackground(Void... params) {
            Cursor cursor = movContext.getContentResolver().query(
                    MovieContract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS, null, null, null
            );
            return getFavoriteMoviesDataFromCursor(cursor);
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                if (movGridAdapter != null) {
                    movGridAdapter.setData(movies);
                }
                movies = new ArrayList<>();
                movies.addAll(movies);
            }
        }
    }
}
