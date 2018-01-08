package com.coinkarasu.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.coinkarasu.activities.CoinListFragment;
import com.coinkarasu.activities.EditTabsFragment;
import com.coinkarasu.activities.HomeTabFragment;
import com.coinkarasu.activities.etc.NavigationKind;

import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private static final boolean DEBUG = false;
    private static final String TAG = "MainPagerAdapter";

    private Context context;
    private NavigationKind defaultKind;
    private List<NavigationKind> visibleKindsCache;
    private long version;

    public MainPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public MainPagerAdapter(FragmentManager manager, Context context, NavigationKind defaultKind) {
        super(manager);
        this.context = context;
        this.defaultKind = defaultKind;
        this.visibleKindsCache = NavigationKind.visibleValues(context);
        this.version = System.currentTimeMillis();
    }

    @Override
    public Fragment getItem(int position) {
        NavigationKind selectedKind = visibleKindsCache.get(position);
        if (DEBUG) Log.d(TAG, "getItem() " + position + " " + selectedKind.name());

        Fragment fragment;

        switch (selectedKind) {
            case home:
                fragment = HomeTabFragment.newInstance(selectedKind);
                break;
//            case assets:
//                fragment = HomeTabFragment.newInstance(selectedKind);
//                break;
            case edit_tabs:
                fragment = EditTabsFragment.newInstance();
                break;
            default:
                fragment = CoinListFragment.newInstance(selectedKind, defaultKind == selectedKind);
        }

        return fragment;
    }

    /**
     * notifyDataSetChangedが呼ばれた後、すべてのアイテムに対してgetItemPositionが呼ばれ、
     * その時にPOSITION_NONEが返されたobjectのみ、getItemIdとgetItemが順番に呼ばれる。
     */
    @Override
    public int getItemPosition(Object object) {
        if (DEBUG) Log.d(TAG, "getItemPosition() " + object.toString());
        return POSITION_NONE;
    }

    public void notifyTabChanged(NavigationKind kind, boolean isAdded) {
        version = System.currentTimeMillis();
        visibleKindsCache = NavigationKind.visibleValues(context);
        super.notifyDataSetChanged();
    }

    /**
     * FragmentPagerAdapterの中で、Fragmentのタグに利用されている。
     * getItemIdが変わった場合のみ、次にgetItemが呼ばれる。
     * <p>
     * 同じfragmentインスタンスを使い続ける場合、タグを変えることはできない。
     * タグを変えないようにgetItemIdが同じ値を返すようにすると、今度はgetItemが呼ばれない。
     * よって、タブの追加/削除を行う場合は、必ず新しいタグとfragmentインスタンスを作る必要がある。
     **/
    @Override
    public long getItemId(int position) {
        if (DEBUG) Log.d(TAG, "getItemId() " + position + " " + version);
        return position + version;
    }

    /**
     * 何もしていなくても呼ばれ続ける時があるため、キャッシュのサイズを返すようにしている。
     */
    @Override
    public int getCount() {
        return visibleKindsCache.size();
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}