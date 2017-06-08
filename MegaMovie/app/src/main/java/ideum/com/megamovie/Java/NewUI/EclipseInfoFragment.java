package ideum.com.megamovie.Java.NewUI;

import android.Manifest;
import android.content.Context;
import android.location.Location;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;


import ideum.com.megamovie.Java.Application.MyApplication;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeCalculator;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimeLocationManager;
import ideum.com.megamovie.Java.LocationAndTiming.GPSFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyMapFragment;
import ideum.com.megamovie.Java.LocationAndTiming.MyTimer;
import ideum.com.megamovie.Java.LocationAndTiming.EclipsePath;
import ideum.com.megamovie.Java.LocationAndTiming.EclipseTimingMap;
import ideum.com.megamovie.R;

public class EclipseInfoFragment extends Fragment
        implements LocationSource.OnLocationChangedListener,
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
    private EclipseTimeLocationManager mEclipseTimeManager;
    private MyTimer mTimer;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mViewPager;

    private MyMapFragment mMyMapFragment;
    private CountdownFragment mCountdownFragment;
    private PhasesFragment mPhasesFragment;

    private Long c1ContactTime;
    private Long c2ContactTime;
    private Long cmContactTime;
    private Long c3ContactTime;
    private Long c4ContactTime;
    private Long millsToC2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eclipse_info, container, false);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        mMyMapFragment = (MyMapFragment) mSectionsPagerAdapter.getItem(0);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle(getString(R.string.eclipse_info_section_title));
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
        EclipseTimeCalculator eclipseTimeCalculator = ma.getEclipseTimeCalculator();


        mEclipseTimeManager = new EclipseTimeLocationManager(eclipseTimeCalculator,getActivity().getApplicationContext());
        mEclipseTimeManager.setAsLocationListener(mGPSFragment);

//        MyMapFragment mmf = (MyMapFragment) mSectionsPagerAdapter.getItem(0);
        //mEclipseTimeManager.setPlannedLatLngProvider(mmf);
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
        mmf.setCurrentLatLng(new LatLng(mLocation.getLatitude(),mLocation.getLongitude()));
        CountdownFragment cdf = (CountdownFragment) mSectionsPagerAdapter.getItem(1);
        double distance = EclipsePath.distanceToPathOfTotality(location);
        cdf.setDistanceToPathOfTotality(distance);
    }

    @Override
    public void onTick() {

        if (mEclipseTimeManager == null) {
            return;
        }

        Long timeRemaining = mEclipseTimeManager.getTimeToEclipse(EclipseTimingMap.Event.CONTACT2);
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

            // Todo: refactor this code
            Long c1Time = mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT1);

            boolean c1Changed = false;
            if (c1ContactTime == null) {
                if (c1Time != null) {
                    c1Changed = true;
                }
            } else {
                c1Changed = !c1ContactTime.equals(c1Time);
            }
            if (c1Changed) {
                c1ContactTime = c1Time;
                mPhasesFragment.setC1Mills(c1ContactTime);

            }

            Long c2Time = mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT2);

            boolean c2Changed = false;
            if (c2ContactTime == null) {
                if (c2Time != null) {
                    c2Changed = true;
                }
            } else {
                c2Changed = !c2ContactTime.equals(c2Time);
            }
            if (c2Changed) {
                c2ContactTime = c2Time;
                mPhasesFragment.setC2Mills(c2ContactTime);
            }

            Long cmTime = mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.MIDDLE);

            boolean cmChanged = false;
            if (cmContactTime == null) {
                if (cmTime != null) {
                    cmChanged = true;
                }
            } else {
                cmChanged = !cmContactTime.equals(cmTime);
            }
            if (cmChanged) {
                cmContactTime = cmTime;
                mPhasesFragment.setCmMills(cmContactTime);
            }


            Long c3Time = mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT3);
            boolean c3Changed = false;
            if (c3ContactTime == null) {
                if (c3Time != null) {
                    c3Changed = true;
                }
            } else {
                c3Changed = !c3ContactTime.equals(c3Time);
            }
            if (c3Changed) {
                c3ContactTime = c3Time;
                mPhasesFragment.setC3Mills(c3ContactTime);
                Log.i("EclipseInfoFrag","c3 changed");
            }

            Long c4Time = mEclipseTimeManager.getEclipseTime(EclipseTimingMap.Event.CONTACT4);
            boolean c4Changed = false;
            if (c4ContactTime == null) {
                if (c4Time != null) {
                    c4Changed = true;
                }
            } else {
                c4Changed = !c4ContactTime.equals(c4Time);
            }
            if (c4Changed) {
                c4ContactTime = c4Time;
                mPhasesFragment.setC4Mills(c4ContactTime);

            }
        }
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
}
