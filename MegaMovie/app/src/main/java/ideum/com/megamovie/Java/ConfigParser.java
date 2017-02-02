package ideum.com.megamovie.Java;


import android.content.res.XmlResourceParser;
import android.support.annotation.XmlRes;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigParser {
    private Map<String,String> configMap;

    public ConfigParser(XmlResourceParser parser) {

        configMap = parseMap(parser);
    }

    public CaptureSequence.CaptureSettings getSettings() {
        int sensitivity = Integer.parseInt(configMap.get("SENSOR_SENSITIVITY"));
        long duration = Long.parseLong(configMap.get("SENSOR_EXPOSURE_TIME"));
        float focus = Float.parseFloat(configMap.get("LENS_FOCUS_DISTANCE"));

        return new CaptureSequence.CaptureSettings(duration,sensitivity,focus);
    }
    public int[] getCaptureSpacing() {
        int num1 = Integer.parseInt(configMap.get("CAPTURE_SPACING_1"));
        int num2 = Integer.parseInt(configMap.get("CAPTURE_SPACING_2"));
        int num3 = Integer.parseInt(configMap.get("CAPTURE_SPACING_3"));
        int num4 = Integer.parseInt(configMap.get("CAPTURE_SPACING_4"));
        int num5 = Integer.parseInt(configMap.get("CAPTURE_SPACING_5"));

        return new int[]{num1, num2, num3, num4, num5};
    }


    public Map<String,String> parseMap(XmlResourceParser parser) {
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
