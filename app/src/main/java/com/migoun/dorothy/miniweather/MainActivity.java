package com.migoun.dorothy.miniweather;
//TODO: exception handling for when network is available but needs authentication or graphical.weather.gov is down
//TODO: try to cut down on the intent/sharedpreferences duality. it's weird and inefficient.
//TODO: no text is being displayed for metric switch
//TODO: two icons on the app screen?? what's up with that??
//TODO: refresh after the user signals they're done with the ZIP
//TODO: some sort of progress bar while waiting for Networking
//TODO: actually have an app icon other than the android head
//TODO: add semaphores for weather ? (because async task is kinda another thread)
//TODO: what's up with the wide variations in memory? One night it's 9mb, another it's 3. (switching b/w activities??)

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    protected boolean unit;
    protected String ZIP;
    static protected GetWeather weather;
    protected Calendar today;
    public static final String PREFS_NAME = "UnitsAndZip";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weather = new GetWeather();

        Intent intent = getIntent();
        if (intent.getStringExtra("ZIP") == null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
            ZIP = prefs.getString("zip", "21228");
            unit = prefs.getBoolean("units", false);
        } else {
            ZIP = intent.getStringExtra("ZIP");
            unit = intent.getBooleanExtra("UNIT", false);
        }

        EditText zipedit = (EditText) findViewById(R.id.zipEdit);
        zipedit.setText(ZIP);

        if (isNetworkAvailable()) {
            today = Calendar.getInstance();
            String now = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR));
            today.add(Calendar.DAY_OF_MONTH, 2);
            String later = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR)); // add one to get 7 instead of 6 lows

            (new Networking()).execute(ZIP, now, later, "e");

            Switch metricToImperial = (Switch) findViewById(R.id.unitSwitch);
            metricToImperial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    unit = isChecked;
                    refresh(findViewById(R.id.unitSwitch));
                }
            });
        } else
            networkError(this);
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();

        editor.putBoolean("units", unit);
        editor.putString("zip", ZIP);

        editor.commit();
    }


    public void refresh(View view) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();

        editor.clear();

        editor.putBoolean("units", unit);
        editor.putString("zip", ZIP);

        editor.commit();

        if (isNetworkAvailable()) {
            today = Calendar.getInstance();
            String now = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR));
            today.add(Calendar.DAY_OF_MONTH, 1);
            String later = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR));

            ZIP = ((EditText) findViewById(R.id.zipEdit)).getText().toString();


            if (unit)
                (new Networking()).execute(ZIP, now, later, "m");
            else
                (new Networking()).execute(ZIP, now, later, "e");
        }

    }

    protected void updateText() {
        if (weather.networkError)
            networkError(this);
        else {
            Resources res = getResources();
            String speed = res.getString(weather.getParsed().get(0).speedUnit());
            String temp = res.getString(weather.getParsed().get(0).tempUnit());

            ((TextView) findViewById(R.id.currentTemp)).setText(String.format(res.getString(R.string.current_temperature),
                    weather.getParsed().get(0).getCurrent(), temp));
            ((TextView) findViewById(R.id.high)).setText(String.format(res.getString(R.string.current_high),
                    weather.getParsed().get(0).getHigh(), temp));
            ((TextView) findViewById(R.id.low)).setText(String.format(res.getString(R.string.current_low),
                    weather.getParsed().get(0).getLow(), temp));
            ((TextView) findViewById(R.id.precip)).setText(String.format(res.getString(R.string.current_precip),
                    weather.getParsed().get(0).getPrecipitation(), "%"));
            ((TextView) findViewById(R.id.cloud)).setText(String.format(res.getString(R.string.current_clouds),
                    weather.getParsed().get(0).cloudiness()));
            ((TextView) findViewById(R.id.wind)).setText(String.format(res.getString(R.string.current_wind),
                    weather.getParsed().get(0).getSpeed(), speed, res.getString(weather.getParsed().get(0).windiness())));
            ((TextView) findViewById(R.id.humidity)).setText(String.format(res.getString(R.string.current_humidity),
                    weather.getParsed().get(0).getHumidity(), "%"));
        }
    }

    public void sevenDay(View view) {
        Intent intent = new Intent(this, SevenDayActivity.class);
        intent.putExtra("UNIT", unit).putExtra("ZIP", ZIP);
        startActivity(intent);
    }

    protected boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                                    .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void networkError(Context ct) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ct);
        builder.setCancelable(false);
        builder.setMessage(String.format(getString(R.string.no_network), weather.exceptionMessage));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
            }
        });
        try {
            AlertDialog dialog = builder.create();
            dialog.show();
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Can't create handler inside thread that has not called Looper.prepare()"))
                Looper.prepare();
        }
    }

    private class Networking extends AsyncTask<String, Void, GetWeather> {
        GetWeather fin;

        protected GetWeather doInBackground(String... params) {
            try {
                fin = new GetWeather(params[0], params[1], params[2], params[3]);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            onPostExecute();
            return fin;
        }

        protected void onPostExecute() {
            handler.sendMessage(Message.obtain(handler, 0, fin));
        }
    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            weather = msg.obj == null ? weather : (GetWeather) msg.obj;
            updateText();
        }
    };
}
