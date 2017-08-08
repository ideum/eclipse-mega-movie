package ideum.com.megamovie.Java.NewUI.EclipseDay;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import ideum.com.megamovie.R;

public class EclipseDayMainActivity extends Activity
implements FragmentManager.OnBackStackChangedListener{

    private Class initialFragmentClass = EclipseDayMyEclipseActivity.class;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eclipse_day_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Keep phone from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        loadInitialFragment(initialFragmentClass);
    }

    public void loadFragment(Class c) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

       FragmentManager fragmentManager = getFragmentManager();
        Fragment current = fragmentManager.findFragmentByTag("current");

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (current != null) {
            transaction.remove(current);
        }
        transaction.add(R.id.flContent, fragment, "current");
       transaction.addToBackStack(null);
        transaction.commit();

    }


    private void loadInitialFragment(Class c) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.flContent, fragment, "current");
        transaction.commit();

    }

    @Override
    public void onBackStackChanged() {

    }
}
