package ideum.com.megamovie.Java.NewUI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import ideum.com.megamovie.R;

public class IntroActivity extends AppCompatActivity
        implements ViewPager.OnTouchListener,
        GoogleApiClient.OnConnectionFailedListener,
        Button.OnClickListener,
        ViewPager.OnPageChangeListener {


    private static final int RC_SIGN_IN = 9001;
    private int REQUEST_LOCATION_PERMISSIONS = 0;

    private GoogleApiClient mGoogleApiClient;

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
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

//        ImageButton rightArrow = (ImageButton) findViewById(R.id.right_chevron);
//        rightArrow.setOnClickListener(this);


//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();
//
        signInButton = (Button) findViewById(R.id.get_started_button);
        signInButton.setOnClickListener(this);
        signInButton.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {
        loadMainActivity();
//        loadMainActivity();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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


//    @Override
//    public void onClick(View v) {
//        int currentPage = mViewPager.getCurrentItem();
//        if (currentPage < mSectionsPagerAdapter.getCount() - 1) {
//            mViewPager.setCurrentItem(currentPage + 1);
//        } else {
//            loadMainActivity();
//        }
//    }

    private void loadMainActivity() {
        startActivity(new Intent(this, MainActivity.class));

    }

    private void loadSignInActivity() {
        startActivity(new Intent(this, SignInActivity.class));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

//        switch (event.getAction()) {
//            case: MotionEvent.
//        }

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
            String[] content_strings = {"On August 21st, 2017, a solar eclipse will be visible in North America. People in the narrow <b>path of totality</b>\n" +
                    "        will see the sun\'s disk completely covered by the moon. This app makes it easy\n" +
                    "        to capture images of the eclipse. Your images can also become part of an exciting citizen science project.",
                    getString(R.string.intro_text_1),
                    getString(R.string.intro_text_2),
                    ""
            };
            textView.setText(Html.fromHtml(content_strings[sectionNumber]));
            Resources res = getResources();

            TextView sectionTitle = (TextView) rootView.findViewById(R.id.section_title);
            String[] title_strings = {getString(R.string.intro_title_0),
                    getString(R.string.intro_title_1),
                    getString(R.string.intro_title_2),
                    ""};
            sectionTitle.setText(title_strings[sectionNumber]);

            int[] colors = {res.getColor(R.color.intro_color_1),
                    res.getColor(R.color.intro_color_2),
                    res.getColor(R.color.intro_color_3),
                    res.getColor(R.color.intro_color_4)};

            rootView.setBackgroundColor(colors[sectionNumber]);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.intro_image);

            int[] images = {res.getIdentifier("megamovie_logo_padding", "drawable", getActivity().getPackageName()),
                    res.getIdentifier("megamovie_intro_one", "drawable", getActivity().getPackageName()),
                    res.getIdentifier("megamovie_intro_two_padding", "drawable", getActivity().getPackageName()),
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
