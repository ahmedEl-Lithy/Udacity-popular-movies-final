package com.example.lithy.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.text.DateFormat;

import com.bumptech.glide.Glide;
import com.linearlistview.LinearListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailActivityFragment extends Fragment {

    public static final String TAG = DetailActivityFragment.class.getSimpleName();

    static final String DETAIL_MOVIE = "DETAIL_MOVIE";

    private Movie theMovie;
    private ImageView imgview;

    private TextView movTitle;
    private TextView movOverview;
    private TextView movDate;
    private TextView movVote;

    private LinearListView movTrailer;
    private LinearListView movReview;

    private CardView reviewCard;
    private CardView trailerCard;

    private TrailerAdapter trailerAdapter;
    private ReviewAdapter reviewAdapter;

    private ScrollView movDetail;

    private Toast movToast;

    private ShareActionProvider movShareActionProvider;

    private Trailer movtrailer;

    public DetailActivityFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
        if (theMovie != null) {
            inflater.inflate(R.menu.menu_detail, menu);

            final MenuItem action_favorite = menu.findItem(R.id.action_favorite);
                  MenuItem action_share = menu.findItem(R.id.action_share);

            new AsyncTask<Void, Void, Integer>() {

                @Override
                protected Integer doInBackground(Void... params) {
                    return Utility.isFavorited(getActivity(), theMovie.getId());
                }

                @Override
                protected void onPostExecute(Integer isFavorited) {
                    action_favorite.setIcon(isFavorited == 1 ?
                            R.drawable.abc_btn_rating_star_on_mtrl_alpha :
                            R.drawable.abc_btn_rating_star_off_mtrl_alpha);
                }
            }.execute();

            movShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_share);

            if (movtrailer != null) {
                movShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_favorite:
                if (theMovie != null) {
                    // check if movie is in favorites or not
                    new AsyncTask<Void, Void, Integer>() {

                        @Override
                        protected Integer doInBackground(Void... params) {
                            return Utility.isFavorited(getActivity(), theMovie.getId());
                        }

                        @Override
                        protected void onPostExecute(Integer isFavorited) {
                            // if it is in favorites
                            if (isFavorited == 1) {
                                // delete from favorites
                                new AsyncTask<Void, Void, Integer>() {
                                    @Override
                                    protected Integer doInBackground(Void... params) {
                                        return getActivity().getContentResolver().delete(
                                                MovieContract.MovieEntry.CONTENT_URI,
                                                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                                new String[]{Integer.toString(theMovie.getId())}
                                        );
                                    }

                                    @Override
                                    protected void onPostExecute(Integer rowsDeleted) {
                                        item.setIcon(R.drawable.abc_btn_rating_star_off_mtrl_alpha);
                                        if (movToast != null) {
                                            movToast.cancel();
                                        }
                                        movToast = Toast.makeText(getActivity(), getString(R.string.removed_from_favorites), Toast.LENGTH_SHORT);
                                        movToast.show();
                                    }
                                }.execute();
                            }
                            // if it is not in favorites
                            else {
                                // add to favorites
                                new AsyncTask<Void, Void, Uri>() {
                                    @Override
                                    protected Uri doInBackground(Void... params) {
                                        ContentValues values = new ContentValues();

                                        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, theMovie.getId());
                                        values.put(MovieContract.MovieEntry.COLUMN_TITLE, theMovie.getTitle());
                                        values.put(MovieContract.MovieEntry.COLUMN_POSTER, theMovie.getposter());
                                        values.put(MovieContract.MovieEntry.COLUMN_BACKDROP, theMovie.getbackdrop());
                                        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, theMovie.getOverview());
                                        values.put(MovieContract.MovieEntry.COLUMN_RATING, theMovie.getRating());
                                        values.put(MovieContract.MovieEntry.COLUMN_DATE, theMovie.getDate());

                                        return getActivity().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,
                                                values);
                                    }

                                    @Override
                                    protected void onPostExecute(Uri returnUri) {
                                        item.setIcon(R.drawable.abc_btn_rating_star_on_mtrl_alpha);
                                        if (movToast != null) {
                                            movToast.cancel();
                                        }
                                        movToast = Toast.makeText(getActivity(), getString(R.string.added_to_favorites), Toast.LENGTH_SHORT);
                                        movToast.show();
                                    }
                                }.execute();
                            }
                        }
                    }.execute();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            theMovie = arguments.getParcelable(DetailActivityFragment.DETAIL_MOVIE);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        movDetail = (ScrollView) rootView.findViewById(R.id.detail_layout);

        if (theMovie != null) {
            movDetail.setVisibility(View.VISIBLE);
        } else {
            movDetail.setVisibility(View.INVISIBLE);
        }

        imgview = (ImageView) rootView.findViewById(R.id.detail_image);
        movTitle = (TextView) rootView.findViewById(R.id.detail_title);
        movOverview = (TextView) rootView.findViewById(R.id.detail_overview);
        movDate = (TextView) rootView.findViewById(R.id.detail_date);
        movVote = (TextView) rootView.findViewById(R.id.detail_vote_average);

        movTrailer = (LinearListView) rootView.findViewById(R.id.detail_trailers);
        movReview = (LinearListView) rootView.findViewById(R.id.detail_reviews);

        reviewCard = (CardView) rootView.findViewById(R.id.detail_reviews_cardview);
        trailerCard = (CardView) rootView.findViewById(R.id.detail_trailers_cardview);

        trailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
        movTrailer.setAdapter(trailerAdapter);

        movTrailer.setOnItemClickListener(new LinearListView.OnItemClickListener() {
            @Override
            public void onItemClick(LinearListView linearListView, View view,
                                    int position, long id) {
                Trailer trailer = trailerAdapter.getItem(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
                startActivity(intent);
            }
        });

        reviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
        movReview.setAdapter(reviewAdapter);

        if (theMovie != null) {

            String image_url = Utility.buildImageUrl(342, theMovie.getbackdrop());

            Glide.with(this).load(image_url).into(imgview);

            movTitle.setText(theMovie.getTitle());
            movOverview.setText(theMovie.getOverview());

            DateFormat dateFormat = DateFormat.getDateInstance();
            String releaseDate = theMovie.getDate();
            releaseDate = releaseDate.replaceAll("-", "/");
            String formattedDate = dateFormat.format(new Date(releaseDate));
            movDate.setText(formattedDate);

            movVote.setText(theMovie.getRating() + "/10");
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (theMovie != null) {
            new FetchTrailersTask().execute(Integer.toString(theMovie.getId()));
            new FetchReviewsTask().execute(Integer.toString(theMovie.getId()));
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, theMovie.getTitle() + " " +
                "http://www.youtube.com/watch?v=" + movtrailer.getKey());
        return shareIntent;
    }

    public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

        private List<Trailer> getTrailersDataFromJson(String jsonStr) throws JSONException {
            JSONObject trailerJson = new JSONObject(jsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray("results");

            List<Trailer> results = new ArrayList<>();

            for(int i = 0; i < trailerArray.length(); i++) {
                JSONObject trailer = trailerArray.getJSONObject(i);
                if (trailer.getString("site").contentEquals("YouTube")) {
                    Trailer trailerModel = new Trailer(trailer);
                    results.add(trailerModel);
                }
            }
            return results;
        }

        @Override
        protected List<Trailer> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
                return getTrailersDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

           return null;
        }

        @Override
        protected void onPostExecute(List<Trailer> trailers) {
            if (trailers != null) {
                if (trailers.size() > 0) {
                    trailerCard.setVisibility(View.VISIBLE);
                    if (trailerAdapter != null) {
                        trailerAdapter.clear();
                        for (Trailer trailer : trailers) {
                            trailerAdapter.add(trailer);
                        }
                    }

                    movtrailer = trailers.get(0);
                    if (movShareActionProvider != null) {
                        movShareActionProvider.setShareIntent(createShareMovieIntent());
                    }
                }
            }
        }
    }

    public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {

        private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

        private List<Review> getReviewsDataFromJson(String jsonStr) throws JSONException {
            JSONObject reviewJson = new JSONObject(jsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray("results");

            List<Review> results = new ArrayList<>();

            for(int i = 0; i < reviewArray.length(); i++) {
                JSONObject review = reviewArray.getJSONObject(i);
                results.add(new Review(review));
            }

            return results;
        }

        @Override
        protected List<Review> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
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
                return getReviewsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Review> reviews) {
            if (reviews != null) {
                if (reviews.size() > 0) {
                    reviewCard.setVisibility(View.VISIBLE);
                    if (reviewAdapter != null) {
                        reviewAdapter.clear();
                        for (Review review : reviews) {
                            reviewAdapter.add(review);
                        }
                    }
                }
            }
        }
    }
}