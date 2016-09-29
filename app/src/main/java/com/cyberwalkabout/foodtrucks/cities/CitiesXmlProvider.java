package com.cyberwalkabout.foodtrucks.cities;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.uki.foodtrucks.R;


import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

public class CitiesXmlProvider implements CitiesProvider {

    private static final String TAG = CitiesXmlProvider.class.getSimpleName();
    private static final String TAG_CITY = "city";

    private WeakReference<Context> ctxRef;
    private List<City> cities;

    public CitiesXmlProvider(Context ctx) {
        ctxRef = new WeakReference<Context>(ctx);
    }

    public List<City> getCities() {
        return cities;
    }

    @Override
    public boolean load() {
        XmlResourceParser xml = ctxRef.get().getResources().getXml(R.xml.cities);
        cities = new ArrayList<City>();
        try {
            City city = null;
            while (xml.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xml.getEventType() == XmlResourceParser.START_TAG) {
                    if (TAG_CITY.equals(xml.getName())) {
                        city = new City();
                        city.setLatitude(Double.parseDouble(xml.getAttributeValue(null, "lat")));
                        city.setLongitude(Double.parseDouble(xml.getAttributeValue(null, "lon")));
                        city.setName(xml.getAttributeValue(null, "name"));
                        city.setId(xml.getAttributeValue(null, "worksheet_id"));
                        cities.add(city);
                    }
                }
                xml.next();
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

}
