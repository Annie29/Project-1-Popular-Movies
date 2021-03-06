/*
 * Copyright (C) 2015 Laurie White (copyright@lauriewhite.org)
 *
 * Project based on Project Sunshine from Udacity's "Developing
 * Android Apps" course at
 * https://www.udacity.com/course/developing-android-apps--ud853
 *
 */


package com.example.android.project1movies.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    private ShareActionProvider mShareActionProvider;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Somewhere in the application.
    public void doShare(Intent shareIntent) {
        // When you want to share set the share intent.
//        mShareActionProvider.setShareIntent(shareIntent);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        public final String LOG_TAG = DetailActivity.class.getSimpleName();

        private MovieData mCurrentMovie;

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);

        }

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();

            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                mCurrentMovie = (MovieData) intent.getParcelableExtra(Intent.EXTRA_TEXT);
                ((TextView) rootView.findViewById(R.id.detail_title))
                        .setText(mCurrentMovie.getTitle());
                ((TextView) rootView.findViewById(R.id.detail_release_date))
                        .setText("Release date: " + mCurrentMovie.getReleaseDate());
                //  TODO: Consider figuring out how to draw stars.
                ((TextView) rootView.findViewById(R.id.detail_vote_average))
                        .setText("Vote average: " + mCurrentMovie.getVoteAverage()
                                + "/10 of " + mCurrentMovie.getVotes() + " votes");
                ((TextView) rootView.findViewById(R.id.detail_plot_synopsis))
                        .setText("Overview: " + mCurrentMovie.getOverview());
                //  TODO: Handle cases with no posters in this size.
                String url = rootView.getContext().getString(R.string.API_URL_image_base)
                        + rootView.getContext().getString(R.string.API_image_size_detail)
                        + mCurrentMovie.getPosterURL();
                ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_poster);
                Picasso.with(rootView.getContext())
                        .load(url)
                        .into(imageView);

            }

            return rootView;
        }

    }
}
