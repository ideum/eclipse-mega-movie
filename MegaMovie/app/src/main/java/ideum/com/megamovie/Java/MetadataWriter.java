/**
 * Simple class for writing camera metadata to
 * an xml file
 */

package ideum.com.megamovie.Java;

import android.hardware.camera2.CaptureResult;

public class MetadataWriter {
    private CaptureResult mCaptureResult;
    private String timeStamp;

    public static final CaptureResult.Key[] RESULT_KEYS =
            new CaptureResult.Key[]{
                    CaptureResult.SENSOR_SENSITIVITY,
                    CaptureResult.SENSOR_EXPOSURE_TIME,
                    CaptureResult.LENS_FOCUS_DISTANCE,
                    CaptureResult.JPEG_GPS_LOCATION};

    public MetadataWriter(CaptureResult captureResult, String timestamp) {
        mCaptureResult = captureResult;
        timeStamp = timestamp;
    }

    public String getXMLString() {

        String str = "<image time=\"";
        if (timeStamp != null) {
            str += timeStamp;
        }
        str += "\">\n";
        for (CaptureResult.Key key : RESULT_KEYS) {
            if (mCaptureResult.get(key) == null) {
                continue;
            }
            str += "\t<";
            str += key.getName();
            str += ">";
            str += mCaptureResult.get(key).toString();
            str += "<\\";
            str += key.getName();
            str += ">\n";
        }
       str += "<\\image>\n";


        return str;
    }

}
