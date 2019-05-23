package ideum.com.eclipsecamera2019.Java.LocationAndTiming;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;


import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;


import org.json.JSONException;
import org.json.JSONObject;

import ideum.com.eclipsecamera2019.R;

public class MyMapFragment extends Fragment
        implements OnMapReadyCallback,
        View.OnClickListener,
        PlaceSelectionListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LatLng currentLatLng;
    private LatLng plannedLatLng;


    // Parameters for initial camera position and zoom level
    private LatLng initialPoint = new LatLng(-36, -58);
    private float initialZoom = 3.2f;


    public MyMapFragment() {
        // Required empty public constructor
    }

    public static MyMapFragment newInstance() {
        MyMapFragment fragment = new MyMapFragment();

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_my_map, container, false);

        FragmentManager fm = getChildFragmentManager();


        SupportMapFragment smf = (SupportMapFragment) fm.findFragmentByTag("mapFragment");
        if (smf == null) {
            smf = new SupportMapFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.myMap_fragment_holder, smf, "mapFragment");
            ft.commit();
            fm.executePendingTransactions();
        }

        smf.getMapAsync(this);

        FloatingActionButton myLocationButton = (FloatingActionButton) rootView.findViewById(R.id.my_location_fab);
        myLocationButton.setOnClickListener(this);

        SupportPlaceAutocompleteFragment paf = (SupportPlaceAutocompleteFragment) fm.findFragmentByTag("autocompleteFragment");
        if (paf == null) {
            paf = new SupportPlaceAutocompleteFragment();
            if (paf != null) {
                paf.setOnPlaceSelectedListener(this);
            }
            FragmentTransaction ft1 = fm.beginTransaction();
            ft1.add(R.id.auto_complete_fragment, paf, "autocompleteFragment");
            ft1.commit();
            fm.executePendingTransactions();

        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        plannedLatLng= getPlannedLocationFromPreferences(context);
        if (plannedLatLng == null && currentLatLng != null) {
            setPlannedLocation(EclipsePath.closestPointOnPathOfTotality(currentLatLng),context);
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentManager fm = getChildFragmentManager();
        SupportPlaceAutocompleteFragment paf = (SupportPlaceAutocompleteFragment) fm.findFragmentByTag("autocompleteFragment");
        if (paf != null) {
            paf.setHint(getString(R.string.search_bar_hint));
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        drawEclipsePath();

        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPoint));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(initialZoom));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if (EclipsePath.distanceToPathOfTotality(latLng) <= 0) {
                    setPlannedLocation(latLng,getContext());
                    showLocationInPathSelectedToast();
                    drawPathToPlannedLocation();
                } else {
                    showLocationNotInPathToast();
                }
            }
        });

        drawPathToPlannedLocation();
    }

    private void setTimeZonePreferenceString(LatLng latLng,Context context) {
        if(context == null) {
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = String.format("https://maps.googleapis.com/maps/api/timezone/json?location=%f,%f&timestamp=1458000000&key=AIzaSyB0mm9X7tEIxtV-2DAS1LRMhmhGwLQPl-8", latLng.latitude, latLng.longitude);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject != null) {
                        String timeZoneId = jsonObject.getString("timeZoneId");
                        if (timeZoneId == null) {
                            return;
                        }
                        Activity currentActivity = getActivity();
                        if (currentActivity == null) {
                            return;
                        }
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(currentActivity.getApplicationContext());
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(getString(R.string.timezone_id), timeZoneId);
                        editor.commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }

    // called when the 'current location' button is pressed
    @Override
    public void onClick(View v) {
        // refreshMarkersAndOverlay();
        showDistanceToPathToast();
        setPlannedLocation(EclipsePath.closestPointOnPathOfTotality(currentLatLng),getContext());
        drawPathToPlannedLocation();
    }

    public void setCurrentLatLng(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        currentLatLng = latLng;
        if(plannedLatLng == null) {
            setPlannedLocation(EclipsePath.closestPointOnPathOfTotality(currentLatLng),getContext());
        }
    }

    private void setPlannedLocation(LatLng latLng,Context context) {
        if (latLng == null) {
            return;
        }
        //Only allow locations within the path of totality
        if (EclipsePath.distanceToPathOfTotality(latLng) > 0.1) {
            showLocationNotInPathToast();
            return;
        }

        plannedLatLng = latLng;

        setPlannedLocationPreferenceValue(latLng,context);
    }

    private void setPlannedLocationPreferenceValue(LatLng latLng,Context context) {
        if (latLng == null || context == null) {
            return;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(getString(R.string.planned_lng_key), (float) latLng.longitude);
        editor.putFloat(getString(R.string.planned_lat_key), (float) latLng.latitude);
        editor.commit();
        setTimeZonePreferenceString(latLng,context);
    }


    private void setCurrentLocationMarker() {
        if (currentLatLng == null || mMap == null) {
            return;
        }
        mMap.addMarker(new MarkerOptions().position(currentLatLng).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.current_location_dot)));
    }

    private void refreshMarkersAndOverlay() {
        if (mMap == null) {
            return;
        }
        mMap.clear();
        placeMarkerAtPlannedLocation();
        setCurrentLocationMarker();
        drawEclipsePath();
    }

    private LatLng getPlannedLocationFromPreferences(Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        float lat = settings.getFloat(getString(R.string.planned_lat_key), 0);
        float lng = settings.getFloat(getString(R.string.planned_lng_key), 0);
        LatLng result = null;
        if (lat != 0 && lng != 0) {
            result = new LatLng(lat, lng);
        }
        return result;
    }

    private void placeMarkerAtPlannedLocation() {
        if (plannedLatLng == null) {
            return;
        }
        mMap.addMarker(new MarkerOptions().position(plannedLatLng));
    }

    private void showDistanceToPathToast() {
        if (currentLatLng == null) {
            return;
        }
        double distanceKm = EclipsePath.distanceToPathOfTotality(currentLatLng);
        String toastMessage = String.format(getResources().getString(R.string.distance_from_path_toast), distanceKm);
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void showLocationInPathSelectedToast() {
        if (currentLatLng == null || plannedLatLng == null) {
            return;
        }
        double distanceKm = EclipsePath.greatCircleDistance(currentLatLng, plannedLatLng);
        String message = getResources().getString(R.string.not_in_path_toast_1);
        String toastMessage = String.format(message, distanceKm);
        Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    private void showLocationNotInPathToast() {
        Toast.makeText(getContext(), getResources().getString(R.string.not_in_path_toast), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaceSelected(Place place) {
        if (place == null) {
            return;
        }
        if (EclipsePath.distanceToPathOfTotality(place.getLatLng()) <= 0) {
            showLocationInPathSelectedToast();
            setPlannedLocation(place.getLatLng(),getContext());
            drawPathToPlannedLocation();
        }
    }

    @Override
    public void onError(Status status) {
        Log.d("place error", status.toString());
    }

    private void drawEclipsePath() {

        // Draw path region
        int fillColor = 0x55000066;
        PolygonOptions polygonOptions = new PolygonOptions().strokeColor(Color.BLACK).fillColor(fillColor).strokeWidth(2);

        int numPoints = 500;
        for (int i = 0; i <= numPoints; i++) {
            polygonOptions.add(EclipsePath.getLatLngForParameter(i * 1.0 / numPoints, EclipsePath.NORTH_BOUNDARY));
        }

        for (int i = numPoints; i >= 0; i--) {
            polygonOptions.add(EclipsePath.getLatLngForParameter(i * 1.0 / numPoints, EclipsePath.SOUTH_BOUNDARY));
        }
        mMap.addPolygon(polygonOptions);

        // Draw centerline


        PolylineOptions polylineOptions = new PolylineOptions().color(getResources().getColor(R.color.centerline)).width(3);
        for (int i = 0; i <= numPoints; i++) {
            polylineOptions.add(EclipsePath.getLatLngForParameter(i * 1.0 / numPoints, EclipsePath.CENTER_LINE));
        }
        mMap.addPolyline(polylineOptions);

    }

    private void drawPathToPlannedLocation() {
        if (mMap == null) {
            return;
        }
        if (currentLatLng == null || plannedLatLng == null) {
            return;
        }
        refreshMarkersAndOverlay();
        PolylineOptions plo = new PolylineOptions();
        plo.add(currentLatLng);
        plo.add(plannedLatLng);
        plo.geodesic(true);
        plo.width(5);
        plo.color(getResources().getColor(R.color.intro_color_3));
        mMap.addPolyline(plo);

    }




    @Override
    public boolean onMarkerClick(Marker marker) {
        // marker.remove();
        return true;
    }
}
