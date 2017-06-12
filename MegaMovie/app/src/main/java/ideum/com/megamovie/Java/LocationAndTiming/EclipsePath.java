package ideum.com.megamovie.Java.LocationAndTiming;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Created by MT_User on 5/10/2017.
 */

public class EclipsePath {

    // Number of sample point to calculate closest point in path of totality
    private static final int NUM_SAMPLE_POINTS = 10000;

    private static final double RADIUS_EARTH_KM = 6371;

    public static final int SOUTH_BOUNDARY = 0;
    public static final int NORTH_BOUNDARY = 1;

//    private static final double s0 = 209.07217;
//    private static final double s1 = 11.207175;
//    private static final double s2 = 0.24183656;
//    private static final double s3 = 0.0023871380;
//    private static final double s4 = 0.000011327374;
//    private static final double s5 = 0.000000021152977;

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
            double result = s0 + s1 * Math.pow(lng,1.0) + s2 * Math.pow(lng,2.0) + s3 * Math.pow(lng,3.0);// + s4 * Math.pow(lng,4.0) + s5 * Math.pow(lng,5.0);;
            return result;
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

    // Formula for the distance (in km) between two points on the earth's surface,
    // travelling along a great circle (geodesic distance)

    public static double greatCircleDistance(LatLng p_1, LatLng p_2) {
        double psi_1 = Math.toRadians(p_1.latitude);
        double psi_2 = Math.toRadians(p_2.latitude);
        double lambda_1 = Math.toRadians(p_1.longitude);
        double lambda_2 = Math.toRadians(p_2.longitude);
        double dPsi = psi_2 - psi_1;
        double dLambda = lambda_2 - lambda_1;

        double a = Math.sin(dPsi/2)*Math.sin(dPsi/2) + Math.cos(psi_1)*Math.cos(psi_2)*Math.sin(dLambda/2)*Math.sin(dLambda/2);
        double b = 2 * Math.asin(Math.sqrt(a));

        return RADIUS_EARTH_KM * b;
    }

    public static LatLng closestPointOnPathOfTotality(LatLng pos) {
        if (pos == null) {
            return null;
        }
        LatLng southBoundaryPoint = closestPointOnBoundary(pos,SOUTH_BOUNDARY);
        if (pos.latitude <= southBoundaryPoint.latitude) {
            return southBoundaryPoint;
        }

        LatLng northBoundaryPoint = closestPointOnBoundary(pos,NORTH_BOUNDARY);
        if (pos.latitude >= northBoundaryPoint.latitude) {
            return northBoundaryPoint;
        }

        return pos;
    }

    public static Double distanceToPathOfTotality(Location loc) {
        LatLng latLng = new LatLng(loc.getLatitude(),loc.getLongitude());
        return distanceToPathOfTotality(latLng);
    }

    public static Double distanceToPathOfTotality(LatLng pos) {
        LatLng closestPoint = closestPointOnPathOfTotality(pos);
        return greatCircleDistance(pos,closestPoint);
    }

    private static LatLng closestPointOnBoundary(LatLng pos, int boundary) {


        double parameter = 0;
        LatLng endpoint = getLatLngForParameter(parameter,boundary);
        double minDistance = greatCircleDistance(pos,endpoint);
        double minParameter = 0;

        while(parameter < 1 ) {
            endpoint = getLatLngForParameter(parameter,boundary);
            double distance = greatCircleDistance(pos,endpoint);
            if (distance < minDistance) {
                minDistance = distance;
                minParameter = parameter;
            }
            parameter = parameter + 1.0/NUM_SAMPLE_POINTS;
        }

        return getLatLngForParameter(minParameter,boundary);
    }

}
