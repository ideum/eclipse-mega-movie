package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

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
    private static final double[] SOUTH_BOUNDARY_COEFFS = {-5.5536e+01,-2.7494e-01,-1.2773e-03,-6.1105e-05,-3.8590e-07};
    private static final double[] CENTERLINE_COEFFS = {-6.7982e+01,-1.0794e+00,-2.0007e-02,-2.5639e-04,-1.1500e-06};
    private static final double[] NORTH_BOUNDARY_COEFFS = {1.6155e+01,4.1655e+00,1.0321e-01,1.0270e-03,3.8562e-06};


    private static final double minLongitude = -72;
    private static final double minLongitudeUpperBoundary = - 71.339;
    private static final double minLongitudeLowerBoundary = -71.405;
    private static final double maxLongitude = -57.6;

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

    public static LatLng getLandLatLngForParameter(double t,int boundary) {
        double minLng = boundary == SOUTH_BOUNDARY? minLongitudeLowerBoundary : minLongitudeUpperBoundary;

        double lng = minLng + t * (maxLongitude - minLng);
        double lat = getLatForLng(lng,boundary);
        return new LatLng(lat,lng);
    }



    // Formula for the distance (in km) between two points on the earth's SOUTH_BOUNDARY_COEFFS surface,
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
        LatLng endpoint = getLandLatLngForParameter(parameter,boundary);
        double minDistance = greatCircleDistance(pos,endpoint);
        double minParameter = 0;

        while(parameter < 1 ) {
            endpoint = getLandLatLngForParameter(parameter,boundary);
            double distance = greatCircleDistance(pos,endpoint);
            if (distance < minDistance) {
                minDistance = distance;
                minParameter = parameter;
            }
            parameter = parameter + 1.0/NUM_SAMPLE_POINTS;
        }

        return getLandLatLngForParameter(minParameter,boundary);
    }

}
