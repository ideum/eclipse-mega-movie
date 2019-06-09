package ideum.com.eclipsecamera2019.Java.NewUI;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import ideum.com.eclipsecamera2019.Java.CameraControl.CameraHardwareCheckFragment;
import ideum.com.eclipsecamera2019.R;

/**
 * This activity is shown once, the first time the application is opened. It is launched from
 * MainActivity
 */
public class IntroActivity extends AppCompatActivity
        implements ViewPager.OnTouchListener,
        GoogleApiClient.OnConnectionFailedListener,
        Button.OnClickListener,
        ViewPager.OnPageChangeListener,
        AlertDialog.OnDismissListener {


    private CameraHardwareCheckFragment mCameraHardwareCheckFragment;


    private static final int RC_SIGN_IN = 9001;


    /**
     * Permissions required to take a picture.
     */


    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
    }

    private boolean hasAllPermissionsGranted() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Button signInButton;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        if (!hasAllPermissionsGranted()) {
            requestPermissions();
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_dots);
        tabLayout.setupWithViewPager(mViewPager);

        signInButton = (Button) findViewById(R.id.get_started_button);
        signInButton.setOnClickListener(this);
        signInButton.setVisibility(View.GONE);

        mCameraHardwareCheckFragment = new CameraHardwareCheckFragment();
        getSupportFragmentManager().beginTransaction().add(mCameraHardwareCheckFragment, "hardwareCheckFragment").commit();

    }

    private void displayCameraNotSupportedWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.camera_not_supported_warning))
                .setPositiveButton("Got It", null)
                .setCancelable(false)
                .setOnDismissListener(this);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (CameraHardwareCheckFragment.isCameraSupported()) {
            displayCameraNotSupportedWarning();
        } else {
            loadMainActivity();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Log.d("TAG", acct.getEmail());

        }
        loadMainActivity();
    }


    private void loadMainActivity() {
        startActivity(new Intent(this, MainActivity.class));

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        int currentPage = mViewPager.getCurrentItem();
        if (currentPage < mSectionsPagerAdapter.getCount() - 1) {
            mViewPager.setCurrentItem(currentPage + 1);
        } else {
            loadMainActivity();
        }
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 2) {
            signInButton.setVisibility(View.VISIBLE);
        } else {
            signInButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        loadMainActivity();
    }


    public static class IntroFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public IntroFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static IntroFragment newInstance(int sectionNumber) {
            IntroFragment fragment = new IntroFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            TextView textView = (TextView) rootView.findViewById(R.id.section_text);
            Resources res = getResources();
            int[] textColors = {res.getColor(R.color.intro_text_color_1),
                    res.getColor(R.color.intro_text_color_2),
                    res.getColor(R.color.intro_text_color_3),
                    res.getColor(R.color.intro_text_color_4)};

            String[] content_strings = {getString(R.string.intro_text),
                    getString(R.string.intro_text_1),
                    getString(R.string.intro_text_2),
                    ""
            };
            textView.setText(Html.fromHtml(content_strings[sectionNumber]));
            textView.setTextColor(textColors[sectionNumber]);

            TextView sectionTitle = (TextView) rootView.findViewById(R.id.section_title);
            String[] title_strings = {getString(R.string.intro_title_0),
                    getString(R.string.intro_title_1),
                    getString(R.string.intro_title_2),
                    ""};
            sectionTitle.setText(title_strings[sectionNumber]);
            sectionTitle.setTextColor(textColors[sectionNumber]);
            int[] colors = {res.getColor(R.color.intro_color_1),
                    res.getColor(R.color.intro_color_2),
                    res.getColor(R.color.intro_color_3),
                    res.getColor(R.color.intro_color_4)};


            rootView.setBackgroundColor(colors[sectionNumber]);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.intro_image);

            int[] images = {res.getIdentifier("intro_1", "drawable", getActivity().getPackageName()),
                    res.getIdentifier("intro_2", "drawable", getActivity().getPackageName()),
                    res.getIdentifier("intro_3", "drawable", getActivity().getPackageName()),
                    0};
            imageView.setImageResource(images[sectionNumber]);


            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a IntroFragment (defined as a static inner class below).
            return IntroFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "";
                case 1:
                    return "";
                case 2:
                    return "";
            }
            return null;
        }
    }
}
