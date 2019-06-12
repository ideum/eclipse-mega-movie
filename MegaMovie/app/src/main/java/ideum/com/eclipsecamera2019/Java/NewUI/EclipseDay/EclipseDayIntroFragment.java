package ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay;

import android.content.DialogInterface;
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

import ideum.com.eclipsecamera2019.Java.Application.CustomNamable;
import ideum.com.eclipsecamera2019.Java.CameraControl.CameraHardwareCheckFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

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
            Toast.makeText(getActivity(),getString(R.string.in_path_alert),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(),getString(R.string.not_in_path_warning),Toast.LENGTH_SHORT).show();
        }

        return rootView;


    }

    private boolean checkCameraSupported()  {
        return CameraHardwareCheckFragment.isCameraSupported();

    }

    public void showSafetyWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources()
                .getString(R.string.safety_warning))
                .setTitle(getResources().getString(R.string.safety_warning_title))
                .setPositiveButton(getString(R.string.got_it), new DialogInterface.OnClickListener() {
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
        builder.setMessage(getResources().getString(R.string.unsupported_camera_message))
                .setPositiveButton("Ok", null)
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
    public int getTitleId() {
        return R.string.eclipse_day_intro_fragment_title;
    }
}
