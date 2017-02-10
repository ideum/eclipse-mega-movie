package ideum.com.megamovie.Java;


import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ideum.com.megamovie.R;

public class ConfigParser {
    //    private Map<String, String> configMap;
    private Resources mResources;

    public ConfigParser(Resources resources) {
        mResources = resources;
//        configMap = parseConfig(parser);
    }

    public List<CaptureSequence.IntervalProperties> getIntervalProperties() throws IOException, XmlPullParserException {
        XmlResourceParser parser = mResources.getXml(R.xml.config);
        return readIntervals(parser);
    }


    private List<CaptureSequence.IntervalProperties> readIntervals(XmlResourceParser parser) throws XmlPullParserException, IOException {
        List<CaptureSequence.IntervalProperties> properties = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("interval_properties")) {
                properties.add(readIntervalProperties(parser));
            }
        }
        return properties;
    }

    private CaptureSequence.IntervalProperties readIntervalProperties(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, null, "interval_properties");
        Integer sensitivity = null;
        Long exposureTime = null;
        Float focusDistance = null;
        Long spacing = null;
        Boolean shouldSaveRaw = null;
        Boolean shouldSaveJpeg = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            if (parser.getName().equals("sensor_sensitivity")) {
                sensitivity = readInteger(parser);
            }
            if (parser.getName().equals("sensor_exposure_time")) {
                exposureTime = readLong(parser);
            }
            if (parser.getName().equals("lens_focus_distance")) {
                focusDistance = readFloat(parser);
            }
            if (parser.getName().equals("spacing")) {
                spacing = readLong(parser);
            }
            if (parser.getName().equals("should_save_raw")) {
                shouldSaveRaw = readBoolean(parser);
            }
            if (parser.getName().equals("should_save_jpeg")) {
                shouldSaveJpeg = readBoolean(parser);
            }
        }
        return new CaptureSequence.IntervalProperties(sensitivity,
                exposureTime,
                focusDistance,
                spacing,
                shouldSaveRaw,
                shouldSaveJpeg);
    }

    private Integer readInteger(XmlPullParser parser) throws IOException, XmlPullParserException {
        String integerString = readText(parser);
        return Integer.parseInt(integerString);
    }

    private Long readLong(XmlPullParser parser) throws IOException, XmlPullParserException {
        String longString = readText(parser);
        return Long.parseLong(longString);
    }

    private Float readFloat(XmlPullParser parser) throws IOException, XmlPullParserException {
        String floatString = readText(parser);
        return Float.parseFloat(floatString);
    }

    private Boolean readBoolean(XmlPullParser parser) throws IOException, XmlPullParserException {
        String booleanString = readText(parser);
        return Boolean.parseBoolean(booleanString);
    }


    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }


    private StringPair readStringPair(XmlPullParser parser) throws IOException, XmlPullParserException {
        String key = parser.getAttributeValue(null, "key");
        parser.next();
        String value = parser.getText();
        parser.next();
        return new StringPair(key, value);
    }

    private static class StringPair {
        public String key;
        public String value;

        public StringPair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
