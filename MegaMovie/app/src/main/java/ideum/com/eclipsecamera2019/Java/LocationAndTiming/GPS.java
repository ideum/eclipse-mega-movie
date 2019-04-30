package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

/**
 * Created by MT_User on 8/11/2017.
 */

public class GPS {
    private static StringBuilder sb = new StringBuilder(20);
    /**
     * returns ref for latitude which is S or N.
     *
     * @param latitude
     * @return S or N
     */
    public static String latitudeRef(final double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }


    public static String longitudeRef(final double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    public static final String convert(double latitude) {
        latitude = Math.abs(latitude);
        final int degree = (int)latitude;
        latitude *= 60;
        latitude -= degree * 60.0d;
        final int minute = (int)latitude;
        latitude *= 60;
        latitude -= minute * 60.0d;
        final int second = (int)(latitude * 1000.0d);
        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }
}
