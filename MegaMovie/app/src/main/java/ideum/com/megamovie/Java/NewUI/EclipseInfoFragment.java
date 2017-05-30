package ideum.com.megamovie.Java.NewUI;

import android.Manifest;
import android.location.Location;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.LocationListener;


import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.Java.PatagoniaTest.EclipseTimeCalculator;
import ideum.com.megamovie.Java.PatagoniaTest.GPSFragment;
import ideum.com.megamovie.Java.PatagoniaTest.MyTimer;
import ideum.com.megamovie.Java.Utility.EclipsePath;
import ideum.com.megamovie.Java.Utility.EclipseTimingMap;
import ideum.com.megamovie.R;

public class EclipseInfoFragment extends Fragment
       implements LocationListener,
        MyTimer.MyTimerListener {

    /**
     * Request code for permissions
     */
    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private GPSFragment mGPSFragment;
    private Location mLocation;
    private EclipseTimeCalculator mEclipseTimeCalculator;
    private MyTimer mTimer;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mViewPager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eclipse_info, container, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.view_pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // Disables page navigation by swiping
         mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        // Set up tab layout to sync with the ViewPager
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton assistantButton = (FloatingActionButton) rootView.findViewById(R.id.assistant_fab);
        assistantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.onAssistantButtonPressed(v);
            }
        });


        return rootView;
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        private MyMapFragment mMapFragment;
        private CountdownFragment mCountdownFragment;
        private PhasesFragment mPhasesFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mMapFragment = MyMapFragment.newInstance();
            mCountdownFragment = CountdownFragment.newInstance();
            mPhasesFragment = PhasesFragment.newInstance();
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return mMapFragment;
            } else if (position == 1) {
                return mCountdownFragment;
            } else {
                return mPhasesFragment;
            }

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
                    return "MAP";
                case 1:
                    return "COUNTDOWN";
                case 2:
                    return "PHASES";
            }
            return null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        MyMapFragment mmf = (MyMapFragment) mSectionsPagerAdapter.getItem(0);
        mmf.setLocation(mLocation);
        CountdownFragment cdf = (CountdownFragment) mSectionsPagerAdapter.getItem(1);
        double distance = EclipsePath.distanceToPathOfTotality(location);
        cdf.setDistanceToPathOfTotality(distance);
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new MyTimer();
        mTimer.addListener(this);
        mTimer.startTicking();

        // Add GPS fragment
        mGPSFragment = new GPSFragment();
        getActivity().getFragmentManager().beginTransaction().add(
                android.R.id.content, mGPSFragment).commit();
        mGPSFragment.addLocationListener(this);



        MyApplication ma = (MyApplication) getActivity().getApplication();
        EclipseTimingMap etm = ma.getEclipseTimingMap();
        // Create the EclipseTimeCalculator
        mEclipseTimeCalculator = new EclipseTimeCalculator(etm, mGPSFragment);

    }

    @Override
    public void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onPause();
    }

    @Override
    public void onTick() {
        if (mEclipseTimeCalculator == null) {
            return;
        }
        Long mills = mEclipseTimeCalculator.getTimeToEvent(EclipseTimingMap.Event.CONTACT2);

        if (mills == null) {
            return;
        }

        CountdownFragment cdf = (CountdownFragment) mSectionsPagerAdapter.getItem(1);
        cdf.setMillsRemaining(mills);
    }
}
