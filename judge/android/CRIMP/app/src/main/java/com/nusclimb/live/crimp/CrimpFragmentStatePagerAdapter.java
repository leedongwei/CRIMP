package com.nusclimb.live.crimp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nusclimb.live.crimp.hello.CrimpFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass of {@link android.support.v4.app.FragmentStatePagerAdapter
 * FragmentStatePagerAdapter} that allows dynamic adding and removing of
 * {@link android.support.v4.app.Fragment Fragment} to a
 * FragmentStatePagerAdapter. CrimpFragmentStatePagerAdapter manages a
 * list of Fragments to populate pages inside of a
 * {@link android.support.v4.view.ViewPager ViewPager}.
 *
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class CrimpFragmentStatePagerAdapter extends FragmentStatePagerAdapter{
    private final String TAG = "FragStatePagerAdapter";

    private List<CrimpFragment> fragmentList;

    /**
     * Constructs a CrimpFragmentStatePagerAdapter object with an empty list
     * of Fragments.
     *
     * @param fm Interface for interacting with Fragments inside
     *           CrimpFragmentStatePagerAdapter.
     */
    public CrimpFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragmentList = new ArrayList<CrimpFragment>();
    }

    /**
     * Constructs a CrimpFragmentStatePagerAdapter object with the
     * {@code fragmentList} given.
     *
     * @param fm Interface for interacting with Fragments inside
     *           CrimpFragmentStatePagerAdapter.
     * @param fragmentList List of Fragments to populate a ViewPager.
     */
    public CrimpFragmentStatePagerAdapter(FragmentManager fm, List<CrimpFragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        // This method name is misleading. This method will be call when
        // FragmentStatePagerAdapter needs an item and it does not exist. This
        // method instantiate the item.

        return fragmentList.get(position);
    }

    @Override
    public int getItemPosition(Object item) {
        // This method is how we achieve adding and removing of Fragments.
        // Search for item in fragmentList and return POSITION_NONE if it
        // is not found (removed from fragmentList).

        int position = fragmentList.indexOf((Fragment)item);
        if(position == -1){
            return POSITION_NONE;
        }
        else{
            return position;
        }
    }

    /**
     * Inserts the specified mFragment at the specified position in the list
     * of Fragments managed by this adapter.
     *
     * @param index Index at which the specified mFragment is to be inserted.
     * @param mFragment Fragment to be inserted.
     */
    public void addFragment( int index, CrimpFragment mFragment) {
        fragmentList.add(index, mFragment);
        notifyDataSetChanged();
    }

    /**
     * Appends the specified mFragment to the end of the list of Fragments
     * managed by this adapter.
     *
     * @param mFragment Fragment to be appended.
     */
    public void addFragment(CrimpFragment mFragment) {
        addFragment(fragmentList.size(), mFragment);
    }

    /**
     * Removes the Fragment at the specified position in the list of Fragments
     * managed by this adapter. Shifts any subsequent Fragments to the left
     * (subtracts one from their indices). Returns the Fragment that was
     * removed from this adapter.
     *
     * @param index The index of the Fragment to be removed.
     */
    public Fragment removeFragment(int index){
        Fragment fragment = fragmentList.remove(index);
        notifyDataSetChanged();

        return fragment;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}