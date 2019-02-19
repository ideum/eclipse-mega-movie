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

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;


import ideum.com.megamovie.Java.Application.CustomNamable;
import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeLocationManager;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimes;
import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyMapFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.LocationAndTiming.EclipsePath;
import ideum.com.megamovie.R;

public class EclipseInfoFragment extends Fragment
        implements LocationSource.OnLocationChangedListener,
        MyTimer.MyTimerListener,
        CustomNamable {

    /**
     * Request code for permissions
     */
    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private GPSFragment mGPSFragment;
    private Location mLocation;
    private EclipseTimeLocationManager mEclipseTimeManager;
    private EclipseTimes mEclipseTimes;
    private MyTimer mTimer;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mViewPager;

    private CountdownFragment mCountdownFragment;
    private PhasesFragment mPhasesFragment;


    private Long millsToC2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eclipse_info, container, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        mCountdownFragment = (CountdownFragment) mSectionsPagerAdapter.getItem(1);
        mPhasesFragment = (PhasesFragment) mSectionsPagerAdapter.getItem(2);

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

    public void refresh() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mCountdownFragment = (CountdownFragment) mSectionsPagerAdapter.getItem(1);
        mPhasesFragment = (PhasesFragment) mSectionsPagerAdapter.getItem(2);
        mViewPager.setAdapter(mSectionsPagerAdapter);
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
        mGPSFragment.activate(this);

        MyApplication ma = (MyApplication) getActivity().getApplication();
        mEclipseTimes = ma.eclipseTimes;
        mEclipseTimeManager = new EclipseTimeLocationManager(mEclipseTimes, getActivity().getApplicationContext());
        mEclipseTimeManager.setAsLocationListener(mGPSFragment);
    }

    @Override
    public void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        MyMapFragment mmf = (MyMapFragment) mSectionsPagerAdapter.getItem(0);
        mmf.setCurrentLatLng(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        CountdownFragment cdf = (CountdownFragment) mSectionsPagerAdapter.getItem(1);
        double distance = EclipsePath.distanceToPathOfTotality(location);
        cdf.setDistanceToPathOfTotality(distance);
        mEclipseTimeManager.calibrateTime(location.getTime());
    }

    @Override
    public void onTick() {

        if (mEclipseTimeManager == null) {
            return;
        }

        Long timeRemaining = mEclipseTimeManager.getTimeToEclipse(EclipseTimes.Phase.c2);

        int item = mViewPager.getCurrentItem();
        if (item == 1) {

            boolean changed = false;
            if (millsToC2 == null) {
                if (timeRemaining != null) {
                    changed = true;
                }
            } else {
                changed = !millsToC2.equals(timeRemaining);
            }
            if (changed) {
                millsToC2 = timeRemaining;
                mCountdownFragment.setMillsRemaining(millsToC2);
            }
        }
        if (item == 2) {
            mPhasesFragment.setContactTimes(mEclipseTimeManager.getEclipseTimes());
        }
    }

    @Override
    public int getTitleId() {
        return R.string.eclipse_info_section_title;
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
                    return getResources().getString(R.string.map);
                case 1:
                    return getResources().getString(R.string.countdown);
                case 2:
                    return getResources().getString(R.string.phases);
            }
            return null;
        }
    }
}
