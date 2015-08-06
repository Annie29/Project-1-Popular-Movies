/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.project1movies.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link ListView} layout.
 */
public class ForecastFragment extends Fragment {

    private MovieCollection mCollection;
//    private String m

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private ArrayAdapter<MovieData> mMovieAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
       // Toast.makeText(getActivity(), "In update weather?!", Toast.LENGTH_LONG);
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(getString(R.string.location_key), "32607");
        String units = sharedPref.getString(getString(R.string.unit_key), "Imperial");
        String sortOrder = sharedPref.getString(getString(R.string.PREF_sort_order_key), getString(R.string.PREF_option_popularity));
        String minVotes = sharedPref.getString(getString(R.string.PREF_minimum_votes_key), getString(R.string.PREF_minimum_votes_default));
        weatherTask.execute(sortOrder, minVotes, location, units);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
/*
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));

        // Now that we have some dummy forecast data, create an ArrayAdapter.
*/
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
/*
        mMovieAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList<String>());
*/
        mMovieAdapter =
                new MyAdapter(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList<MovieData>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        GridView listView = (GridView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mMovieAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //  TODO: Fix this, getting the right things.
                MovieData forecast = mMovieAdapter.getItem(position);
                //Intent intent = new Intent(getActivity(), DetailActivity.class)
                //        .putExtra(Intent.EXTRA_TEXT, forecast);
                //  TODO: Come up with a better name than EXTRA_TEXT
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });

        return rootView;
    }

    //  TODO: Change to TMD API
    public class FetchWeatherTask extends AsyncTask<String, Void, MovieData[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private String moviesJsonStr = null;

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low, String units) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            //  TODO: Figure out how to store @strings in an array
            if (units.equals("Imperial")) {
                high = convertMetricToImperial(high);
                low = convertMetricToImperial(low);
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private double convertMetricToImperial(double temperature) {
            return (temperature * 9.0 / 5.0 + 32.0);
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays, String unitType)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low, unitType);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            return resultStrs;

        }

        @Override
        /**
         * params[0] = sort order
         * params[1] = minimum votes
         */

        protected MovieData[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
           /* if (params.length == 0) {
                return null;
            }
*/
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 14;

/*
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                String unitType = "Metric";
                if (params.length >= 2) {
                    unitType = params[1];
                }
                return getWeatherDataFromJson(forecastJsonStr, numDays, unitType);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }
*/
            try {
                // Construct the URL for the theMovieDB query
                // Possible parameters are available at TMDB's forecast API page, at
                // https://www.themoviedb.org/documentation/api/discover
                final String MOVIE_BASE_URL = getString(R.string.API_URL_base) + getString(R.string.API_discover);
                String sort_order = getString(R.string.PREF_option_popularity);
                if (params.length>0) {
                    sort_order = params[0];
                }

                Log.d(LOG_TAG, "URL !!! is " + MOVIE_BASE_URL);
                //  TODO:  Update this to use shared preferences to get the sort order
                Uri builtUri = Uri.parse(MOVIE_BASE_URL);
                if (sort_order.equals(getString(R.string.PREF_option_popularity))) {
                    builtUri = builtUri.buildUpon()
                            .appendQueryParameter(getString(R.string.API_sort_query),
                                    getString(R.string.API_sort_by_pop)).build();
                 }
                else {
                    builtUri = builtUri.buildUpon()
                            .appendQueryParameter(getString(R.string.API_sort_query),
                                    getString(R.string.API_sort_by_vote)).build();
                    if (sort_order.equals(getString(R.string.PREF_option_rating_50))) {
                        String minVotes = "50";
                        if (params.length > 1) {
                            try {
                                Integer i = new Integer(params[1]);
                                minVotes = params[1];
                            } catch (NumberFormatException e) {
                                //  Well, that wasn't an int, keep the 50.
                            }
                        }
                        builtUri = builtUri.buildUpon()
                                .appendQueryParameter("vote_count.gte",
                                        minVotes).build();

                    }
                }

                builtUri = builtUri.buildUpon().appendQueryParameter(getString(R.string.API_key_query),
                        getString(R.string.API_key))
                        .build();

                URL url = new URL(builtUri.toString());
                Log.d(LOG_TAG, "Final URL !!! is " + url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    Log.d(LOG_TAG, "!!! input Stream is NULL");
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));


                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                    Log.d(LOG_TAG, "Read line!!! " + line);
                }

                Log.d(LOG_TAG, "Done with all reads!!! >" + buffer.toString() + "<");
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
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

            MovieData[] md = null;
            mCollection = new MovieCollection(moviesJsonStr);
            //return mCollection.getData().toArray(md);
            // This will only happen if there was an error getting or parsing the forecast.
            Log.d(LOG_TAG, "!!!! + at end, string is " + moviesJsonStr);
            return null;
        }


        @Override
        protected void onPostExecute(MovieData[] result) {
            super.onPostExecute(result);
//            GridView gridView = (GridView) getActivity().findViewById(R.id.gridview);
//            gridView.setAdapter(new ImageAdapter(getActivity(), movieDatas));
            Log.d(LOG_TAG, "!!!! In onPostExecute");
            //if (result != null) {
                mMovieAdapter.clear();
                for (MovieData dayForecastStr : mCollection) {
                    Log.d(LOG_TAG, "!!!! URL is " + dayForecastStr.getPosterURL());

                    mMovieAdapter.add(dayForecastStr);
                }
                // New data is back from the server.  Hooray!
//            }
        }
//        MovieCollection mCollection = new MovieCollection(moviesJsonStr);
//        GridView gridView = (GridView) getActivity().findViewById(R.id.listview_forecast);
//        gridView.setAdapter(new MyAdapter(mContext, mCollection));
//    }
        //mCollection = movieDatas;
//        mCollection = new MovieCollection(moviesJsonStr);
//        GridView gridView = (GridView) getActivity().findViewById(R.id.listview_forecast);
//        gridView.setAdapter(new MyAdapter(mContext, mCollection));

    }
}


//
//        @Override
//        protected void onPostExecute(String[] result) {
//            if (result != null) {
//                mMovieAdapter.clear();
//                for(String dayForecastStr : result) {
//                    mMovieAdapter.add(dayForecastStr);
//                }
//                // New data is back from the server.  Hooray!
//            }
//        }
//    }
//}