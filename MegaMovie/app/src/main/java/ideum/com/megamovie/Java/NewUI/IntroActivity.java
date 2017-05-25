package ideum.com.megamovie.Java.NewUI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.ButtonBarLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import ideum.com.megamovie.Java.MapActivity;
import ideum.com.megamovie.R;

public class IntroActivity extends AppCompatActivity
implements View.OnClickListener{

    private int REQUEST_LOCATION_PERMISSIONS = 0;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);


//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_dots);
        tabLayout.setupWithViewPager(mViewPager);

        ImageButton rightArrow = (ImageButton) findViewById(R.id.right_chevron);
        rightArrow.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_intro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int currentPage = mViewPager.getCurrentItem();
        if (currentPage < mSectionsPagerAdapter.getCount() - 1) {
            mViewPager.setCurrentItem(currentPage + 1);
        } else {
            loadMainActivity();
        }
    }

    private void loadMainActivity() {
        startActivity(new Intent(this, MainActivity.class));

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
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
            String[] content_strings = {getString(R.string.intro_text_0),
                    getString(R.string.intro_text_1),
                    getString(R.string.intro_text_2)};
            textView.setText(content_strings[sectionNumber]);
            Resources res = getResources();

            TextView sectionTitle = (TextView) rootView.findViewById(R.id.section_title);
            String[] title_strings = {getString(R.string.intro_title_0),
                    getString(R.string.intro_title_1),
                    getString(R.string.intro_title_2)};
            sectionTitle.setText(title_strings[sectionNumber]);

            int[] colors = {res.getColor(R.color.intro_color_1),
            res.getColor(R.color.intro_color_2),
            res.getColor(R.color.intro_color_3)};

            rootView.setBackgroundColor(colors[sectionNumber]);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.intro_image);

            int[] images = {res.getIdentifier("megamovie_logo","drawable", getActivity().getPackageName()),
                    res.getIdentifier("megamovie_intro_one","drawable", getActivity().getPackageName()),
                    res.getIdentifier("megamovie_intro_two","drawable", getActivity().getPackageName())};
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
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
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
