package com.example.android.sunshine.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class ForecastFragment extends Fragment {
//
//        public ForecastFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//
//            ArrayList<String> forecasts = new ArrayList<String>();
//
//            forecasts.add("Today - Sunny 88/63");
//            forecasts.add("Tomorrow - Foggy 70/46");
//            forecasts.add("Wed - Cloudy 72/63");
//            forecasts.add("Thurs - Rainy 64/51");
//            forecasts.add("Fri - Foggy 70/46");
//            forecasts.add("Sat - Sunny 76/68");
//            forecasts.add("Sun - Florida 99/78");
//            forecasts.add("Mon - Florida 98/76");
//            forecasts.add("Tues - Florida 103/81");
//
//            ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forcast,
//                    R.id.list_item_forecast_textview, forecasts);
//            ListView lv = (ListView) rootView.findViewById(R.id.listview_forecast);
//            lv.setAdapter(myAdapter);
//            return rootView;
//        }
//    }
}