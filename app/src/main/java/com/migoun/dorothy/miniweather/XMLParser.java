package com.migoun.dorothy.miniweather;

// XMLParser.java	Dorothy Carter
// this class uses SAX to parse some XML

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

public class XMLParser extends DefaultHandler {
    protected ArrayList<Weather> list;
    boolean grab;
    String what, layout;
    int times;

    public ArrayList<Weather> getList() {
        return list;
    }

    public void startDocument() throws SAXException {
        list = new ArrayList<Weather>();
        grab = false;

        for (int num=0; num<15; num++) {
            list.add(new Weather());
        }
    }

    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes attr) throws SAXException {
        if (qName.equals("layout-key")) {
            grab = true;
            what = "layout-key";
            times = -1;
        } else if (qName.equals("start-valid-time")) {
            grab = true;
            what = "layout";
            times++;
        } else if (qName.equals("temperature")) {
            what = "temperature ";
            what += attr.getValue(0);
            times = -1;
            layout = attr.getValue(2);
        } else if (qName.equals("probability-of-precipitation")) {
            what = "precipitation";
            times = -1;
            layout = attr.getValue(2);
        } else if (qName.equals("wind-speed")) {
            what = "wind speed";
            times = -1;
            layout = attr.getValue(2);
        } else if (qName.equals("direction")) {
            what = "wind direction";
            times = -1;
            layout = attr.getValue(2);
        } else if (qName.equals("cloud-amount")) {
            what = "cloud cover";
            times = -1;
            layout = attr.getValue(2);
        } else if (qName.equals("humidity")) {
            what = "humidity";
            times = -1;
            layout = attr.getValue(2);
        } else if (qName.equals("value")) {
            grab = true;
            times++;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (grab) {
            String temp = "";
            for (int i=start; i<start+length; i++) {
                temp += ch[i];
            }

            if (what.equals("layout-key")) {
                layout = temp;
            } else if (what.equals("layout")) {
                // put the 12-hour timestamps into the weather objects
                if (layout.equals("k-p12h-n14-3"))
                    list.get(times).setTimestamp(temp);

            } else {
                // the value given will be a numeric value
                int number = 0;
                try {
                    number = Integer.parseInt(temp);
                } catch (NumberFormatException e) {}

                if (what.equals("temperature maximum") && times<=6) {
                    list.get(times*2).setHigh(number);
                    list.get((times*2)+1).setHigh(number);

                } else if (what.equals("temperature minimum") && times<=6) {
                    System.out.println("min " + times + " " + number);
                    list.get(times*2).setLow(number);
                    list.get((times*2)+1).setLow(number);

                } else if (what.equals("temperature apparent") && times<=51) {
                    if ((times+1) % 4 == 0 || times==0)
                        list.get((times+1)/4).setCurrent(number);

                } else if (what.equals("precipitation") && times<=13) {
                    list.get(times).setPrecipitation(number);

                } else if (what.equals("wind speed") && times<=51) {
                    if ((times+1) % 4 == 0 || times==0)
                        list.get((times+1)/4).setSpeed(number);

                } else if (what.equals("wind direction") && times<=51) {
                    if ((times+1) % 4 == 0 || times==0)
                        list.get((times+1)/4).setDirection(number);

                } else if (what.equals("cloud cover") && times<=51) {
                    if ((times+1) % 4 == 0 || times==0)
                        list.get((times+1)/4).setCloud(number);

                } else if (what.equals("humidity") && times<=51) {
                    if ((times+1) % 4 == 0 || times==0)
                        list.get((times+1)/4).setHumidity(number);

                }
            }
            grab = false;
        }
    }
}