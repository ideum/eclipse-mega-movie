package ideum.com.megamovie.Java.Utility;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MT_User on 5/10/2017.
 */

public class EclipsePath {

    public static final int SOUTH_BOUNDARY = 0;
    public static final int NORTH_BOUNDARY = 1;

    private static final double s0 = -56.5932;
    private static final double s1 = -1.77885;
    private static final double s2 = -0.00918829;
    private static final double s3 = -0.0000113559;

    private static final double n0 = -57.1030;
    private static final double n1 = -1.84427;
    private static final double n2 = -0.00993789;
    private static final double n3 = -0.0000138865;

    private static final double minLongitude = -126.0;
    private static final double maxLongitude = -78.00;

    public static double getLatForLng(double lng,int boundary) {
        if (boundary == SOUTH_BOUNDARY) {
            return s0 + s1*lng + s2*lng*lng + s3*lng*lng*lng;
        } else if (boundary == NORTH_BOUNDARY) {
            return n0 + n1*lng + n2*lng*lng + n3*lng*lng*lng;
        } else {
            return 0;
        }
    }
    public static LatLng getLatLngForParameter(double t,int boundary) {
        double lng = minLongitude + t * (maxLongitude - minLongitude);
        double lat = getLatForLng(lng,boundary);
        return new LatLng(lat,lng);
    }

}
