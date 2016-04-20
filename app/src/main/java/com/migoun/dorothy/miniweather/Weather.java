package com.migoun.dorothy.miniweather;

// Weather.java     Dorothy Carter
// this class is for storing weather data

class Weather {
    protected String timestamp;
    protected boolean metric;
    protected int hi, lo, current;
    protected int precip, wDirection, wSpeed;
    protected int cloud, relHum;

    public Weather() {
        timestamp = "";
        metric = false;
        hi = 0;
        lo = 0;
        current = 0;
        precip = 0;
        wDirection = 0;
        wSpeed = 0;
        cloud = 0;
        relHum = 0;
    }

    protected int tempUnit() {
        if (metric)
            return R.string.metric_temp;
        else
            return R.string.imperial_temp;
    }

    protected int speedUnit() {
        if (metric)
            return R.string.metric_speed;
        else
            return R.string.imperial_speed;
    }

    public String toString() {
        return hi + " " + lo;
    }

    public String cloudiness() {
        if (cloud <= 10)
            return "clear";
        else if (cloud <= 50)
            return "scattered";
        else if (cloud <= 90)
            return "broken";
        else
            return "overcast";
    }

    public int windiness() {
        int wind;
        if (wDirection <= 45)
            wind = R.string.north;
        else if (wDirection <= 90)
            wind = R.string.northeast;
        else if (wDirection <= 135)
            wind = R.string.east;
        else if (wDirection <= 180)
            wind = R.string.southeast;
        else if (wDirection <= 225)
            wind = R.string.south;
        else if (wDirection <= 270)
            wind = R.string.southwest;
        else if (wDirection <= 315)
            wind = R.string.west;
        else
            wind = R.string.northwest;

        return wind;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean getMetric() {
        return metric;
    }

    public int getHigh() {
        return hi;
    }

    public int getLow() {
        return lo;
    }

    public int getCurrent() {
        return current;
    }

    public int getPrecipitation() {
        return precip;
    }

    public int getDirection() {
        return wDirection;
    }

    public int getSpeed() {
        return wSpeed;
    }

    public int getCloud() {
        return cloud;
    }

    public int getHumidity() {
        return relHum;
    }

    public void setTimestamp(String time) {
        timestamp = time;
    }

    public void setMetric(boolean unit) {
        metric = unit;
    }

    public void setHigh(int newHi) {
        hi = newHi;
    }

    public void setLow(int newLo) {
        lo = newLo;
    }

    public void setCurrent(int newCurrent) {
        current = newCurrent;
    }

    public void setPrecipitation(int newPrecip) {
        precip = newPrecip;
    }

    public void setDirection(int newDirection) {
        wDirection = newDirection;
    }

    public void setSpeed(int newSpeed) {
        wSpeed = newSpeed;
    }

    public void setCloud(int newCloud) {
        cloud = newCloud;
    }

    public void setHumidity(int newHm) {
        relHum = newHm;
    }
}