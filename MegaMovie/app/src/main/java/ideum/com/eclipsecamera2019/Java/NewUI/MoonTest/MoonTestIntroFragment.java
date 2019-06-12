package ideum.com.eclipsecamera2019.Java.NewUI.MoonTest;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ideum.com.eclipsecamera2019.Java.Application.CustomNamable;
import ideum.com.eclipsecamera2019.Java.CameraControl.CameraHardwareCheckFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MainActivity;
import ideum.com.eclipsecamera2019.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoonTestIntroFragment extends Fragment
implements CustomNamable{

    private CameraHardwareCheckFragment mCameraHardwareCheckFragment;


    public MoonTestIntroFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_moon_test_intro, container, false);

        Button getStarted = rootView.findViewById(R.id.start_moon_test);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!checkCameraSupported()) {
                    displayCameraNotSupportedWarning();
                    //return;
                }

                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null) {
                    mainActivity.loadFragment(CompassCalibrationFragment.class);
                }
            }
        });

        mCameraHardwareCheckFragment = new CameraHardwareCheckFragment();
        getChildFragmentManager().beginTransaction().add(mCameraHardwareCheckFragment,"hardwareCheckFragment").commit();

//        String bodyString ="By using the app to capture images of the moon or sun, you'll become familiar with how the\n" +
//                "        app works and be ready to use it to photograph the main event: the solar eclipse.\n" +
//                "        \n\nThis will be especially helpful if you will be using an external lens and tripod during the eclipse. You\n" +
//                "        can find detailed explanations of how to use this equipment in these <a href=\"https://www.youtube.com/watch?v=VhWOx7eW-bI&feature=youtu.be\">video tutorials.</a>";

        String bodyString = getString(R.string.moon_test_intro);

        TextView body = rootView.findViewById(R.id.body);
        body.setText(Html.fromHtml(bodyString));
        body.setMovementMethod(LinkMovementMethod.getInstance());



        return rootView;
    }



    private boolean checkCameraSupported()  {
            return CameraHardwareCheckFragment.isCameraSupported();

    }

    private void displayCameraNotSupportedWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.unsupported_camera_message))
                .setPositiveButton("Ok", null)
                .setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public int getTitleId() {
        return R.string.moon_test_section_title;
    }



}
