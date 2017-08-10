package ideum.com.megamovie.Java.LocationAndTiming;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by MT_User on 5/10/2017.
 */

public class EclipsePath {

    // Number of sample point to calculate closest point in path of totality
    private static final int NUM_SAMPLE_POINTS = 10000;

    private static final double RADIUS_EARTH_KM = 6371;

    public static final int SOUTH_BOUNDARY = 0;
    public static final int CENTER_LINE = 1;
    public static final int NORTH_BOUNDARY = 2;


    // Polynomial coefficients for south boundary, center line, and north boundary of path of totality
    // for latitude as a function of longitude
    private static final double[] SOUTH_BOUNDARY_COEFFS = {209.07217, 11.207175, 0.24183656, 0.0023871380, 1.1327374e-05, 2.1152977e-08};
    private static final double[] CENTERLINE_COEFFS = {195.41727, 10.557370, 0.23023880, 0.0022852060, 1.0882614e-05, 2.0380168e-08};
    private static final double[] NORTH_BOUNDARY_COEFFS = {182.12413, 9.9208717, 0.21882740, 0.0021844822, 1.0441281e-05, 1.9610047e-08};

    private static final double minLongitude = -126.0;
    private static final double maxLongitude = -78.00;

    public static double getLatForLng(double lng,int boundary) {
       double[] coefficients = {};
        if (boundary == SOUTH_BOUNDARY) {
            coefficients = SOUTH_BOUNDARY_COEFFS;
        } else if (boundary == CENTER_LINE) {
            coefficients = CENTERLINE_COEFFS;
        }
        else if (boundary == NORTH_BOUNDARY) {
            coefficients = NORTH_BOUNDARY_COEFFS;
        }
        return evaluatePolynomial(coefficients,lng);
    }

    private static double evaluatePolynomial(double[] c, double t) {
        double result = 0;
        for (int d = 0; d < c.length; d++) {
            result += c[d] * Math.pow(t,d);
        }


        return result;
    }

    public static LatLng getLatLngForParameter(double t,int boundary) {
        double lng = minLongitude + t * (maxLongitude - minLongitude);
        double lat = getLatForLng(lng,boundary);
        return new LatLng(lat,lng);
    }

    // Formula for the distance (in km) between two points on the earth'SOUTH_BOUNDARY_COEFFS surface,
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
