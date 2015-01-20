package de.ifgi.igiapp.igi_app.SharedPreferences;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import de.ifgi.igiapp.igi_app.R;

public class ActivityFirstLaunch extends FragmentActivity {

    private static final int NUM_PAGES = 8;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private PageIndicator mPageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

        //Bind the title indicator to the adapter
        mPageIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mPageIndicator.setViewPager(mViewPager);
    }


    public void onBackPressed(View view) {
        if (mViewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    public void onForwardPressed(View view) {
        if (mViewPager.getCurrentItem() == NUM_PAGES-1) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ScreenSlidePageFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            /*
            Fragment fragment = new ScreenSlidePageFragment();
            Bundle args = new Bundle();
            args.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
            fragment.setArguments(args);
            return fragment;
            */
            Fragment fragment = null;
            switch(position){
                case 0:
                    fragment = new Fragment1();
                    Bundle args1 = new Bundle();
                    args1.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args1);
                    break;

                case 1:
                    fragment = new Fragment2();
                    Bundle args2 = new Bundle();
                    args2.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args2);
                    break;

                case 2:
                    fragment = new Fragment3();
                    Bundle args3 = new Bundle();
                    args3.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args3);
                    break;

                case 3:
                    fragment = new Fragment4();
                    Bundle args4 = new Bundle();
                    args4.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args4);
                    break;

                case 4:
                    fragment = new Fragment5();
                    Bundle args5 = new Bundle();
                    args5.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args5);
                    break;

                case 5:
                    fragment = new Fragment6();
                    Bundle args6 = new Bundle();
                    args6.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args6);
                    break;

                case 6:
                    fragment = new Fragment7();
                    Bundle args7 = new Bundle();
                    args7.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args7);
                    break;

                case 7:
                    fragment = new Fragment8();
                    Bundle args8 = new Bundle();
                    args8.putInt(ScreenSlidePageFragment.ARG_SECTION_NUMBER, position + 1);
                    fragment.setArguments(args8);
                    break;
            }
            return fragment;

        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    /**
     * A fragment representing a section of the app, but that simply
     * displays the page number.
     */
    public static class ScreenSlidePageFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page1, container, false);

            //TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
            //dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            return rootView;
        }
    }

    public static class Fragment1 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page1, container, false);

            return rootView;
        }
    }

    public static class Fragment2 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page2, container, false);

            return rootView;
        }
    }

    public static class Fragment3 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page3, container, false);

            return rootView;
        }
    }

    public static class Fragment4 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page4, container, false);

            return rootView;
        }
    }

    public static class Fragment5 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page5, container, false);

            return rootView;
        }
    }

    public static class Fragment6 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page6, container, false);

            return rootView;
        }
    }

    public static class Fragment7 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page7, container, false);

            return rootView;
        }
    }

    public static class Fragment8 extends Fragment{

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_screen_slide_page8, container, false);

            return rootView;
        }
    }
}
