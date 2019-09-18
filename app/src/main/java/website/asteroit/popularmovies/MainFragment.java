package website.asteroit.popularmovies;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import website.asteroit.popularmovies.utilities.NetworkUtils;

/**
 * Created by Wahyu on 31/07/2017.
 */

public class MainFragment extends Fragment {

    private static final String POPULAR_URL = "https://api.themoviedb.org/3/movie/popular?api_key=" + NetworkUtils.API_KEY;
    private static final String TOP_RATED_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=" + NetworkUtils.API_KEY;

    private Toolbar mMovieListToolbar;
    private AppBarLayout mMovieListAppBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mMovieListToolbar = (Toolbar) rootView.findViewById(R.id.movie_list_toolbar);
        mMovieListAppBar = (AppBarLayout) rootView.findViewById(R.id.movie_list_app_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mMovieListToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.movie_list_view_pager);

        final MovieListPagerAdapter adapter = new MovieListPagerAdapter(getChildFragmentManager());

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.movie_list_tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Fragment fragment = adapter.getFragment(position);

                if (fragment != null) {
                    turnOffToolbarScrolling();
                    MovieListFragment movieListFragment = (MovieListFragment) fragment;
                    movieListFragment.setPositionToTop();
                }
            }
        });

        return rootView;
    }

    public void turnOffToolbarScrolling() {

        //turn off scrolling
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) mMovieListToolbar.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(0);
        mMovieListToolbar.setLayoutParams(toolbarLayoutParams);

        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) mMovieListAppBar.getLayoutParams();
        appBarLayoutParams.setBehavior(null);
        mMovieListAppBar.setLayoutParams(appBarLayoutParams);
    }

    public void turnOnToolbarScrolling() {

        //turn on scrolling
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) mMovieListToolbar.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
        mMovieListToolbar.setLayoutParams(toolbarLayoutParams);

        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) mMovieListAppBar.getLayoutParams();
        appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
        mMovieListAppBar.setLayoutParams(appBarLayoutParams);
    }

    private class MovieListPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<String> mFragmentTags;
        private FragmentManager mFragmentManager;

        public MovieListPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
            mFragmentTags = new SparseArray<>();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return MovieListFragment.newInstance(MovieListFragment.MOST_POPULAR_TAG, POPULAR_URL);
                case 1:
                    return MovieListFragment.newInstance(MovieListFragment.TOP_RATED_TAG, TOP_RATED_URL);
                case 2:
                    return MovieListFragment.newInstance(MovieListFragment.MY_FAVORITES_TAG, null);
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Object obj = super.instantiateItem(container, position);
            if (obj instanceof Fragment) {
                Fragment f = (Fragment) obj;
                String tag = f.getTag();
                mFragmentTags.put(position, tag);
            }
            return obj;
        }

        public Fragment getFragment(int position) {
            String tag = mFragmentTags.get(position);
            if (tag == null) return null;
            return mFragmentManager.findFragmentByTag(tag);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.most_popular);
                case 1:
                    return getString(R.string.top_rated);
                case 2:
                    return getString(R.string.my_favorites);
                default:
                    return super.getPageTitle(position);
            }
        }
    }
}
