package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.CameraControl.CameraHardwareCheckFragment;
import ideum.com.megamovie.Java.NewUI.MainActivity;
import ideum.com.megamovie.R;

public class EclipseDayIntroFragment extends Fragment
implements CustomNamable {

    private CameraHardwareCheckFragment mCameraHardwareCheckFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eclipse_day_intro, container, false);

        mCameraHardwareCheckFragment = new CameraHardwareCheckFragment();
        getChildFragmentManager().beginTransaction().add(mCameraHardwareCheckFragment,"hardwareCheckFragment").commit();


        final Button getStarted = rootView.findViewById(R.id.get_started_button);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkCameraSupported()) {
                    displayCameraNotSupportedWarning();
                    return;
                }
               showSafetyWarning();
            }
        });

        if(checkIfInPath()) {
            Toast.makeText(getActivity(),"You are in the path of totality!",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),"You are not currently in the path of totality!",Toast.LENGTH_SHORT).show();
        }

        return rootView;


    }

    private boolean checkCameraSupported()  {
        return mCameraHardwareCheckFragment.isCameraSupported();

    }

    public void showSafetyWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources()
                .getString(R.string.safety_warning))
                .setTitle(getResources().getString(R.string.safety_warning_title))
                .setPositiveButton("Got It", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getStarted();
                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void displayCameraNotSupportedWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Unfortunately, your phone's camera does not support manual control of its sensors, so you will be unable to take pictures with the app.")
                .setPositiveButton("Got It", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean checkIfInPath() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getBoolean(getString(R.string.in_path_key),false);
    }


    private void getStarted() {
        MainActivity mainActivity = (MainActivity) getActivity();

        mainActivity.loadActivity(EclipseDayMyEclipseActivity.class);
    }

    @Override
    public String getTitle() {
        return "Eclipse Image Capture";
    }
}
