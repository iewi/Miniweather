package com.migoun.dorothy.miniweather;
//TODO: if after 12PM in the day, check i*2 and (i*2)-1
//TODO: what's up with those weird dots in between the days??
//TODO: the low, high, and precip need a bit more space (enough for 4 characters)
//TODO: when going from 1-day to 7-day with metric on, the 7-day does a weird cycling thing

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

public class SevenDayActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.seven_days);

        // get the day headings right
        TextView[] days = {(TextView) findViewById(R.id.thirdday), (TextView) findViewById(R.id.fourthday),
                (TextView) findViewById(R.id.fifthday), (TextView) findViewById(R.id.sixthday),
                (TextView) findViewById(R.id.seventhday)};
        String[] sdays = getResources().getStringArray(R.array.days);

        today = Calendar.getInstance();
        today.add(Calendar.DAY_OF_MONTH, 1);
        for (int i = 0; i < 5; i++) {
            today.add(Calendar.DAY_OF_MONTH, 1);
            days[i].setText(String.format(sdays[i], getString(dayOfWeek(today.get(Calendar.DAY_OF_WEEK))),
                    (today.get(Calendar.MONTH) + 1), today.get(Calendar.DAY_OF_MONTH)));
        }

        Intent intent = getIntent();
        unit = intent.getBooleanExtra("UNIT", false);
        ZIP = intent.getStringExtra("ZIP");

        EditText zipedit = (EditText) findViewById(R.id.zipEdit);
        zipedit.setText(ZIP);

        if (isNetworkAvailable()) {
            today = Calendar.getInstance();
            String now = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR));
            today.add(Calendar.DAY_OF_MONTH, 1);
            String later = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR));

            Switch metricToImperial = (Switch) findViewById(R.id.metric);
            metricToImperial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    unit = isChecked;
                    refresh(findViewById(R.id.metric));
                }
            });

            if (unit) {
                // changed this to an asynchronous task
                (new Networking()).execute(ZIP, now, later, "m");
                metricToImperial.setChecked(true);
            } else
                (new Networking()).execute(ZIP, now, later, "e");

            updateText();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage(R.string.no_network);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    protected void updateText() {
        if (weather.networkError)
            networkError(SevenDayActivity.this);
        else {
            Resources res = getResources();
            String temp = res.getString(weather.getParsed().get(0).tempUnit());

            String[] shighs = res.getStringArray(R.array.highs);
            String[] slows = res.getStringArray(R.array.lows);
            String[] sprecips = res.getStringArray(R.array.precips);

            TextView[] highs = {(TextView) findViewById(R.id.highFirst),(TextView) findViewById(R.id.highSecond),
                    (TextView) findViewById(R.id.highThird), (TextView) findViewById(R.id.highFourth),
                    (TextView) findViewById(R.id.highFifth), (TextView) findViewById(R.id.highSixth),
                    (TextView) findViewById(R.id.highSeventh)};
            TextView[] lows = {(TextView) findViewById(R.id.lowFirst),(TextView) findViewById(R.id.lowSecond),
                    (TextView) findViewById(R.id.lowThird), (TextView) findViewById(R.id.lowFourth),
                    (TextView) findViewById(R.id.lowFifth), (TextView) findViewById(R.id.lowSixth),
                    (TextView) findViewById(R.id.lowSeventh)};
            TextView[] precips = {(TextView) findViewById(R.id.precipFirst),(TextView) findViewById(R.id.precipSecond),
                    (TextView) findViewById(R.id.precipThird), (TextView) findViewById(R.id.precipFourth),
                    (TextView) findViewById(R.id.precipFifth), (TextView) findViewById(R.id.precipSixth),
                    (TextView) findViewById(R.id.precipSeventh)};

            int highest;
            for (int i=0; i<7; i++) {
                highs[i].setText(String.format(shighs[i], weather.getParsed().get(i * 2).getHigh(), temp));
                lows[i].setText(String.format(slows[i], weather.getParsed().get(i * 2).getLow(), temp));
                if (weather.getParsed().get(i * 2).getPrecipitation() >= weather.getParsed().get((i * 2) + 1).getPrecipitation())
                    highest = weather.getParsed().get(i * 2).getPrecipitation();
                else
                    highest = weather.getParsed().get((i * 2) + 1).getPrecipitation();
                precips[i].setText(String.format(sprecips[i], highest, "%"));
            }
        }
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
                    today.get(Calendar.DAY_OF_MONTH)-1, 22);
            today.add(Calendar.DAY_OF_MONTH, 1);
            String later = GetWeather.buildTimestamp(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.HOUR));
            ZIP = ((EditText) findViewById(R.id.zipEdit)).getText().toString();

            if (unit)
                new Networking().execute(ZIP, now, later, "m");
            else
                new Networking().execute(ZIP, now, later, "e");
        }

    }

    public void oneDay(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("UNIT", unit).putExtra("ZIP", ZIP);
        startActivity(intent);
    }

    protected int dayOfWeek(int dayOfWeek) {
        int retVal;
        switch(dayOfWeek) {
            case 1:
                retVal = R.string.sunday;
                break;
            case 2:
                retVal = R.string.monday;
                break;
            case 3:
                retVal = R.string.tuesday;
                break;
            case 4:
                retVal = R.string.wednesday;
                break;
            case 5:
                retVal = R.string.thursday;
                break;
            case 6:
                retVal = R.string.friday;
                break;
            default:
                retVal = R.string.saturday;
        }
        return retVal;
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
}
