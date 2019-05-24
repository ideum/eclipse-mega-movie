package ideum.com.eclipsecamera2019.Java.NewUI;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import hotchemi.android.rate.OnClickButtonListener;
import ideum.com.eclipsecamera2019.Java.Application.CustomNamable;
import ideum.com.eclipsecamera2019.Java.Application.MyApplication;
import ideum.com.eclipsecamera2019.Java.Application.UploadActivity;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.EclipseTimeProvider;
import ideum.com.eclipsecamera2019.Java.LocationAndTiming.MyTimer;
import ideum.com.eclipsecamera2019.Java.NewUI.EclipseDay.EclipseDayIntroFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.MoonTest.MoonTestIntroFragment;
import ideum.com.eclipsecamera2019.Java.NewUI.Orientation.AssistantEquipmentChoiceInfoFragment;
import ideum.com.eclipsecamera2019.R;

import hotchemi.android.rate.AppRate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        CalibrationFragment.OnFragmentInteractionListener,
        MyTimer.MyTimerListener,
        FragmentManager.OnBackStackChangedListener {

    private EclipseTimeProvider eclipseTimeProvider;
    private static final String TAG = "MAIN_ACTIVITY";

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * The first time the app is opened it shows the intro activity, but
         * afterwards skips it
         */

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean previouslyStarted = prefs.getBoolean(getResources().getString(R.string.previously_started_key), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getResources().getString(R.string.previously_started_key), true);
            edit.commit();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
            return;
        }

        AppRate.with(this).setInstallDays(0)
                .setLaunchTimes(2)
                .setRemindInterval(2)
                .setShowLaterButton(true)
                .setDebug(false)
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);

        setContentView(R.layout.activity_main);
        /**
         * Keep activity in portrait mode
         */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /**
         * Get all necessary permission right away. Could wait until they are needed but this makes
         * it easier to avoid bugs the first time the app is opened
         */






        if (!hasAllPermissionsGranted()) {
            requestAllPermissions();
        }

        eclipseTimeProvider = new EclipseTimeProvider();
        getFragmentManager().beginTransaction().add(
                android.R.id.content, eclipseTimeProvider).commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        MyApplication application = (MyApplication) getApplication();

        loadInitialFragment(application.currentFragment);


        // Shows the safety warning once and then not again
        boolean safetyWarningSeen = prefs.getBoolean(getResources().getString(R.string.safety_warning_seen), false);
        if (!safetyWarningSeen) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getResources().getString(R.string.safety_warning_seen), true);
            edit.commit();
            showSafetyWarning();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    public void onAssistantButtonPressed(View view) {

        loadFragment(OrientationIntroFragment.class);
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer == null) {
            return;
        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.path_of_totality, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_info) {
            loadAboutActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.path_of_totality) {
            loadFragment(EclipseInfoFragment.class);
        } else if (id == R.id.assistant) {
            loadFragment(OrientationIntroFragment.class);
        } else if (id == R.id.about_eclipse_app) {
            loadAboutActivity();
        } else if (id == R.id.my_eclipse) {
            loadActivity(MyEclipseActivity.class);
        } else if (id == R.id.image) {

        } else if (id == R.id.full_moon_test) {
            loadFragment(MoonTestIntroFragment.class);
        } else if (id == R.id.eclipse_day) {
            loadFragment(EclipseDayIntroFragment.class);
        } else if (id == R.id.upload_mode) {
            loadActivity(UploadActivity.class);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void loadActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }

    public void loadFragment(Class c) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment current = fragmentManager.findFragmentByTag("current");

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (current != null) {
            transaction.remove(current);
        }
        transaction.add(R.id.flContent, fragment, "current");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // When we load the initial fragment, we don't want to add the transaction to the back stack
    private void loadInitialFragment(Class c) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.flContent, fragment, "current");
        transaction.commit();

        if (fragment instanceof CustomNamable) {
            int titleId = ((CustomNamable) fragment).getTitleId();
            getSupportActionBar().setTitle(getString(titleId));
        }
    }

    public void loadAssistantFragment(int index) throws ClassNotFoundException {
        AssistantEquipmentChoiceInfoFragment fragment = AssistantEquipmentChoiceInfoFragment.newInstance(index);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment current = fragmentManager.findFragmentByTag("current");

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (current != null) {
            transaction.remove(current);
            current.onDestroy();
        }
        transaction.add(R.id.flContent, fragment, "current");
        transaction.addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onBackStackChanged() {
        Fragment current = getSupportFragmentManager().findFragmentByTag("current");
        if (current == null) {
            return;
        }

        if (current instanceof EclipseInfoFragment) {
            EclipseInfoFragment eif = (EclipseInfoFragment) current;
            eif.refresh();

        }

        if (current instanceof CustomNamable) {
            CustomNamable cn = (CustomNamable) current;
            getSupportActionBar().setTitle(getString(cn.getTitleId()));
            getSupportActionBar().show();
        } else {
            getSupportActionBar().hide();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onTick() {

    }

    private static final int REQUEST_PERMISSIONS = 0;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public void showSafetyWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources()
                .getString(R.string.safety_warning))
                .setTitle(getResources().getString(R.string.safety_warning_title))
                .setPositiveButton(getResources().getString(R.string.got_it), null)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void requestAllPermissions() {
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




}
