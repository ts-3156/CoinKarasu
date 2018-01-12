package com.coinkarasu.pagers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.coinkarasu.activities.CoinListFragment;
import com.coinkarasu.activities.EditTabsFragment;
import com.coinkarasu.activities.HomeTabFragment;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.utils.CKLog;

import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter {
    private static final boolean DEBUG = false;
    private static final String TAG = "MainPagerAdapter";

    private NavigationKind selectedKind;
    private List<NavigationKind> visibleKinds;
    private long version;

    public MainPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    public MainPagerAdapter(FragmentManager manager, List<NavigationKind> visibleKinds, NavigationKind defaultKind) {
        super(manager);
        this.selectedKind = defaultKind;
        this.visibleKinds = visibleKinds;
        this.version = System.currentTimeMillis();
    }

    @Override
    public Fragment getItem(int position) {
        NavigationKind targetKind = visibleKinds.get(position);
        if (DEBUG) Log.d(TAG, "getItem() " + position +
                " selected=" + selectedKind.name() + " target=" + targetKind.name());

        Fragment fragment;

        switch (targetKind) {
            case home:
                fragment = HomeTabFragment.newInstance(selectedKind == targetKind);
                break;
//            case assets:
//                fragment = HomeTabFragment.newInstance(selectedKind);
//                break;
            case edit_tabs:
                fragment = EditTabsFragment.newInstance();
                break;
            default:
                fragment = CoinListFragment.newInstance(targetKind, selectedKind == targetKind);
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

    /**
     * タブの追加/削除を行う実装が、どうやっても帯に短したすきに長し状態になるので、
     * fragmentのremoveを強制的に行うことで暫定的に解決している。
     */
    public void removeFragments(FragmentManager manager, ViewPager pager) {
        int count = getCount();
        FragmentTransaction transaction = manager.beginTransaction();
        for (int i = 0; i < count; i++) {
            String tag = "android:switcher:" + pager.getId() + ":" + getItemId(i);
            Fragment fragment = manager.findFragmentByTag(tag);
            if (fragment == null) {
                // 内部実装を直接参照しているので、nullになることはないはず
                RuntimeException ex = new RuntimeException("removeFragments() fragment is null " + tag);
                CKLog.e(TAG, ex);
                throw ex;
            }
            transaction.remove(fragment);
        }
        transaction.commit();
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
        return visibleKinds.size();
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}