package ideum.com.megamovie.Java;


import android.content.res.XmlResourceParser;
import android.support.annotation.XmlRes;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigParser {
    private XmlResourceParser parser;

    public ConfigParser(XmlResourceParser parser) {
        this.parser = parser;
    }
    public CaptureSequence.CaptureSettings parseCaptureSettings() {
        Map<String,String> map = parseMap();
        int sensitivity = Integer.parseInt(map.get("SENSOR_SENSITIVITY"));
        long duration = Long.parseLong(map.get("SENSOR_EXPOSURE_TIME"));
        float focus = Float.parseFloat(map.get("LENS_FOCUS_DISTANCE"));

        return new CaptureSequence.CaptureSettings(duration,sensitivity,focus);
    }


    public Map<String,String> parseMap() {
        Map<String, String> map = new HashMap<>();
        String key = null, value = null;
        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName() != null && parser.getName().equals("entry")) {
                        key = parser.getAttributeValue(null, "key");

                        if (key == null) {
                            parser.close();
                        }
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    if (key != null) {
                        value = parser.getText();
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.getName().equals("entry")) {
                        map.put(key, value);
                        key = null;
                        value = null;
                    }
                }
                try {
                    eventType = parser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return map;
    }
}
