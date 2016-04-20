package com.migoun.dorothy.miniweather;
//TODO: differentiate between different types of error: malformedURL/parser does not mean network
// GetWeather.java	Dorothy Carter
// This class instantiates an object that stores an URL and the content located at that URL
// The URL always points to the REST service of the NDFD

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GetWeather {
    protected URL here;
    protected boolean unit, networkError;
    protected String exceptionMessage;
    protected InputStream in;
    protected ArrayList<Weather> parsed;

    public GetWeather() {
        exceptionMessage = " ";
        networkError = false;

        in = System.in;
        unit = false;
        try {
            buildURL("21228", "2019-01-01T00:00", "2019-01-01T00:00", "e");
        } catch (MalformedURLException e) {}
        parsed = new ArrayList<Weather>();
        for (int num=0; num<14; num++) {
            parsed.add(new Weather());
        }
    }

    public GetWeather(String ZIP, String begin, String end, String units) {
        exceptionMessage = "(you may not be connected)";
        networkError = false;

        unit = units.equals("m");

        try {
            buildURL(ZIP, begin, end, units);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (testCnx()) {
            grabData();
            try {
                parseData();
            } catch (SAXException e) {
                networkError = true;
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                networkError = true;
                e.printStackTrace();
            } catch (IOException e) {
                networkError = true;
                e.printStackTrace();
            }
        } else {
            networkError = true;
            throw new RuntimeException("Connection bad");
        }
    }

    protected boolean testCnx() {
        try {
            HttpURLConnection cnx = (HttpURLConnection) (new URL("http://google.com")).openConnection();

            if (cnx.getResponseCode() == 200 || cnx.getResponseCode() == 302)
                // 200 OK or 302 FOUND are both ok
                return true;
            else {
                System.out.println(cnx.getURL().toString());
                throw new IOException("connection bad: " + cnx.getResponseCode());
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void grabData() {
        try {
            HttpURLConnection cnx = (HttpURLConnection) here.openConnection();
            cnx.connect();

            // if the connection is ok, read the content from it
            // this will only work if the URL is returned as a string like "http://google.com"
            if (cnx.getResponseCode() == 200 && cnx.getURL().toString().contains("graphical.weather.gov"))
                in = cnx.getInputStream();
            else {
                int family = cnx.getResponseCode() / 100;
                if (family == 3)
                    exceptionMessage = "(the web service has moved)";
                else if (family == 4)
                    exceptionMessage = "(a client-side error has occured)";
                else if (family == 5)
                    exceptionMessage = "(a server-side error has occured)";
                System.out.println(cnx.getURL().toString());
                throw new IOException("Connection bad: " + cnx.getResponseCode());
            }
        } catch (MalformedURLException e) {
            networkError = true;
            e.printStackTrace();
        } catch (UnknownHostException e) {
            networkError = true;
            exceptionMessage = "(graphical.weather.gov may be down)";
        } catch (IOException e) {
            // you can assume that if the above block of
            // code throws an exception, it's a network-
            // related one
            networkError = true;
            e.printStackTrace();
        }
    }

    protected void parseData() throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLParser xmlHandle = new XMLParser();

        try {
            saxParser.parse(in, xmlHandle);
            for (Weather w : xmlHandle.getList()) {
                w.setMetric(unit);
            }

            parsed = xmlHandle.getList();
        } catch (IllegalArgumentException e) {
            networkError = true;
        } catch (NullPointerException e) {
            networkError = true;
        }
    }

    protected void buildURL(String ZIP, String begin, String end, String unit) throws MalformedURLException {
        String base = "http://graphical.weather.gov/xml/sample_products/browser_interface/ndfdXMLclient.php?";
        base += "zipCodeList=" + ZIP;
        base += "&product=time-series";
        base += "&begin=" + begin;
        base += "&end=" + end;
        base += "&Unit=" + unit;

        base += "&maxt=maxt"; // max temp
        base += "&mint=mint"; // minimum temp
        base += "&appt=appt"; // apparent temperature
        base += "&pop12=pop12"; // 12-hr probablility of rain
        base += "&sky=sky"; // cloud cover
        base += "&wspd=wspd"; // wind speed
        base += "&wdir=wdir"; // wind direction
        base += "&rh=rh"; // relative humidity

        here = new URL(base);
    }

    public URL getURL() {
        return here;
    }

    public InputStream getContent() {
        return in;
    }

    public ArrayList<Weather> getParsed() {
        return parsed;
    }

    public void setURL(String ZIP, String begin, String end, String unit) throws MalformedURLException {
        buildURL(ZIP, begin, end, unit);
    }

    public void setContent() {
        if (testCnx()) {
            grabData();
            try {
                parseData();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            networkError = true;
            throw new RuntimeException("Connection bad");
        }
    }

    public String toString() {
        return here + "\n" + in;
    }

    // this is a method to build a timestamp that follows the docs's specifications
    // it may or may not be useful
    public static String buildTimestamp(int year, int month, int day, int hour) {
        String temp = year + "-";
        if (month < 10)
            temp += "0" + month;
        else
            temp += month;

        if (day < 10)
            temp += "0" + day;
        else
            temp += day;

        temp += "T";

        if (hour < 10)
            temp += "0" + hour;
        else
            temp += hour;

        temp += ":00";

        return temp;
    }
}