package rocks.crimp.crimp.hello;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rocks.crimp.crimp.hello.route.RouteFragment;
import rocks.crimp.crimp.hello.scan.ScanFragment;
import rocks.crimp.crimp.hello.score.ScoreFragment;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class HelloFragmentAdapter extends FragmentStatePagerAdapter {
    public static final int ROUTE_TAB_POSITION = 0;
    public static final int SCAN_TAB_POSITION = 1;
    public static final int SCORE_TAB_POSITION = 2;

    private static final int COUNT = 3;

    private Fragment[] fragmentArray = new Fragment[COUNT];
    private int mCanDisplay;
    private TabLayout mTabLayout;
    private Context mContext;
    private TextView mRouteTabTextView;
    private TextView mScanTabTextView;
    private View mScoreTabCustomView;

    public HelloFragmentAdapter(FragmentManager fm, TabLayout tabLayout) {
        super(fm);
        mTabLayout = tabLayout;
        mCanDisplay = 0b111;
    }

    public int getCanDisplay(){
        return mCanDisplay;
    }

    public void setCanDisplay(int canDisplay){
        mCanDisplay = canDisplay;

        if(mRouteTabTextView == null){
            // find route tab TextView
            ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
            int tabCount = vg.getChildCount();
            if(tabCount != COUNT){
                throw new IllegalStateException("We should have "+COUNT+" tabs exactly.");
            }

            ViewGroup vgTab = (ViewGroup) vg.getChildAt(ROUTE_TAB_POSITION);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    mRouteTabTextView = (TextView) tabViewChild;
                    break;
                }
            }
        }

        if(mScanTabTextView == null){
            // find scan tab TextView
            ViewGroup vg = (ViewGroup) mTabLayout.getChildAt(0);
            int tabCount = vg.getChildCount();
            if(tabCount != COUNT){
                throw new IllegalStateException("We should have "+COUNT+" tabs exactly.");
            }

            ViewGroup vgTab = (ViewGroup) vg.getChildAt(SCAN_TAB_POSITION);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    mScanTabTextView = (TextView) tabViewChild;
                    break;
                }
            }
        }

        if(mScoreTabCustomView == null){
            TabLayout.Tab scoreTab = mTabLayout.getTabAt(SCORE_TAB_POSITION);
            if(scoreTab == null){
                throw new IllegalStateException("We should have "+COUNT+" tabs exactly");
            }
            mScoreTabCustomView = scoreTab.getCustomView();
            if(mScoreTabCustomView == null){
                throw new IllegalStateException("We should have custom view for score tab");
            }
        }

        if((canDisplay & 0b001) == 0){
            mRouteTabTextView.setVisibility(View.INVISIBLE);
        }
        else{
            mRouteTabTextView.setVisibility(View.VISIBLE);
        }

        if((canDisplay & 0b010) == 0){
            mScanTabTextView.setVisibility(View.INVISIBLE);
        }
        else{
            mScanTabTextView.setVisibility(View.VISIBLE);
        }

        if((canDisplay & 0b100) == 0){
            mScoreTabCustomView.setVisibility(View.INVISIBLE);
        }
        else{
            mScoreTabCustomView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public CharSequence getPageTitle(int position){
        switch(position) {
            case ROUTE_TAB_POSITION: return "Route";
            case SCAN_TAB_POSITION: return "Scan";
            case SCORE_TAB_POSITION: return "Score";
            default: return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object){
        super.destroyItem(container, position, object);
        fragmentArray[position] = null;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case ROUTE_TAB_POSITION:
                fragmentArray[position] = RouteFragment.newInstance(position,
                        getPageTitle(position).toString());
                break;
            case SCAN_TAB_POSITION:
                fragmentArray[position] = ScanFragment.newInstance(position,
                        getPageTitle(position).toString());
                break;
            case SCORE_TAB_POSITION:
                fragmentArray[position] = ScoreFragment.newInstance(position,
                        getPageTitle(position).toString());
                break;
            default:
                return null;
        }

        return fragmentArray[position];
    }

    @Override
    public int getCount() {
        return COUNT;
    }
}
