package com.nusclimb.live.crimp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.nusclimb.live.crimp.hello.HelloActivityFragment;

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

    private List<HelloActivityFragment> fragmentList;
    private FragmentManager mFragmentManager;
    private long baseId = 0;

    /**
     * Constructs a CrimpFragmentStatePagerAdapter object with an empty list
     * of Fragments.
     *
     * @param fm Interface for interacting with Fragments inside
     *           CrimpFragmentStatePagerAdapter.
     */
    public CrimpFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
        this.mFragmentManager = fm;
        this.fragmentList = new ArrayList<HelloActivityFragment>();
    }

    /**
     * Constructs a CrimpFragmentStatePagerAdapter object with the
     * {@code fragmentList} given.
     *
     * @param fm Interface for interacting with Fragments inside
     *           CrimpFragmentStatePagerAdapter.
     * @param fragmentList List of Fragments to populate a ViewPager.
     */
    public CrimpFragmentStatePagerAdapter(FragmentManager fm, List<HelloActivityFragment> fragmentList) {
        super(fm);
        this.mFragmentManager = fm;
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        // This method name is misleading. This method will be call when
        // FragmentStatePagerAdapter needs an item and it does not exist. This
        // method instantiate the item.

        Log.d(TAG, "called getitem() on position: "+position);
        List<Fragment> fragList = mFragmentManager.getFragments();
        int fragListCount = -1;
        if(fragList!=null)
            fragListCount = fragList.size();
        Log.d(TAG, "***getItem() fraglistcount:"+fragListCount);


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
            return POSITION_UNCHANGED;
        }
    }

    /**
     * Inserts the specified mFragment at the specified position in the list
     * of Fragments managed by this adapter.
     *
     * @param index Index at which the specified mFragment is to be inserted.
     * @param mFragment Fragment to be inserted.
     */
    public void addFragment( int index, HelloActivityFragment mFragment) {
        List<Fragment> fragList = mFragmentManager.getFragments();
        int fragListCount = -1;
        if(fragList!=null)
            fragListCount = fragList.size();
        Log.d(TAG, "*** addFrag("+index+") fraglistcount:"+fragListCount);
        fragmentList.add(index, mFragment);
        notifyDataSetChanged();
    }

    /**
     * Appends the specified mFragment to the end of the list of Fragments
     * managed by this adapter.
     *
     * @param mFragment Fragment to be appended.
     */
    public void addFragment(HelloActivityFragment mFragment) {
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