package ideum.com.megamovie.Java;


import android.hardware.camera2.CaptureResult;

public class CaptureMetadataWriter {
    private CaptureResult mCaptureResult;
    private String mImageFileName;

    public static final CaptureResult.Key[] RESULT_KEYS =
            new CaptureResult.Key[]{CaptureResult.SENSOR_SENSITIVITY, CaptureResult.SENSOR_EXPOSURE_TIME, CaptureResult.LENS_FOCUS_DISTANCE};

    public CaptureMetadataWriter(CaptureResult captureResult,String imageFileName) {
        mCaptureResult = captureResult;
        mImageFileName = imageFileName;
    }

    public String getXMLString() {
        String str = "<image fileName=\"" + mImageFileName + "\">\n";
        for (CaptureResult.Key key : RESULT_KEYS) {
            str += "\t<";
            str += key.getName();
            str += ">";
            str += mCaptureResult.get(key).toString();
            str += "<\\";
            str += key.getName();
            str += ">\n";
        }
       str += "<\\image>";


        return str;
    }

}
