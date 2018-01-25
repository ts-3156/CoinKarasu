package com.coinkarasu.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.pagers.MainPagerAdapter;
import com.coinkarasu.tasks.LogTabChangedEventTask;
import com.coinkarasu.utils.CKLog;

import java.util.List;


public class MainFragment extends Fragment implements
        TabLayout.OnTabSelectedListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "MainFragment";
    private static final String STATE_SELECTED_KIND_KEY = "kind";
    private static final String STATE_PAGER_ADAPTER_VERSION = "pagerAdapterVersion";

    private OnFragmentInteractionListener listener;

    private NavigationKind kind;
    private ViewPager pager;
    private boolean areTabsBeingModified;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        kind = NavigationKind.getDefault();
        if (savedInstanceState != null) {
            NavigationKind savedKind = NavigationKind.valueOf(savedInstanceState.getString(STATE_SELECTED_KIND_KEY, kind.name()));
            if (kind != savedKind && savedKind.isVisible(getActivity())) {
                kind = savedKind;
            }
        }

        MainPagerAdapter adapter = new MainPagerAdapter(getChildFragmentManager(), listener.getVisibleKinds(), kind);

        if (savedInstanceState != null) {
            adapter.setVersion(savedInstanceState.getLong(STATE_PAGER_ADAPTER_VERSION, System.currentTimeMillis()));
            if (DEBUG) CKLog.d(TAG, "Set version to PagerAdapter " + adapter.getVersion());
        }

        pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(NavigationKind.values().length);

        areTabsBeingModified = false;
        TabLayout tabs = listener.getTabLayout();
        tabs.setupWithViewPager(pager);
        tabs.addOnTabSelectedListener(this);
        refreshTabTitles(tabs);

        int position = listener.getVisibleKinds().indexOf(kind);
        pager.setCurrentItem(position, false); // 他のアクティビティから戻ってきた時のみ、onTabSelectedが呼ばれる

        return view;
    }

    public void refreshTabTitles(TabLayout tabs) {
        if (tabs == null) {
            return;
        }

        List<NavigationKind> values = listener.getVisibleKinds();
        for (int i = 0; i < values.size(); i++) {
            TabLayout.Tab tab = tabs.getTabAt(i);

            if (tab == null) {
                continue;
            }

            NavigationKind kind = values.get(i);

            if (kind == NavigationKind.edit_tabs) {
                tab.setIcon(R.drawable.ic_tab_playlist_add);
            } else {
                tab.setText(kind.tabStrResId);
            }
        }
    }

    public NavigationKind getCurrentKind() {
        return kind;
    }

    // バックボタンが押された時、NavigationDrawerがクリックされた時にMainViewControllerから呼ばれる
    public void setCurrentKind(NavigationKind kind, boolean smoothScroll) {
        this.kind = kind;
        int position = listener.getVisibleKinds().indexOf(kind);
        pager.setCurrentItem(position, smoothScroll);
        listener.requestRefreshUi(kind);
    }

    @Override
    public void onResume() {
        super.onResume();

        // onCreateViewの中で実行すると、古い端末で正しく動作しないことがある
        listener.requestRefreshUi(kind);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SELECTED_KIND_KEY, kind.name());
        PagerAdapter adapter = pager.getAdapter();
        if (adapter != null) {
            savedInstanceState.putLong(STATE_PAGER_ADAPTER_VERSION, ((MainPagerAdapter) adapter).getVersion());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * 以前はonPageScrollStateChangedを使っていたが、TabLayout.Tabが渡される分こっちが便利なので変えた。
     * スクロールした時とタブをクリックしたときの両方で、onTabSelectedの方が先に呼ばれる。
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (DEBUG) CKLog.d(TAG, "onTabSelected " + tab.getPosition());
        if (areTabsBeingModified) {
            return;
        }

        int position = tab.getPosition();
        pager.setCurrentItem(position, false);
        kind = listener.getVisibleKinds().get(position);
        listener.requestRefreshUi(kind);

        new LogTabChangedEventTask().execute(getActivity(), kind);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    public void refreshTabVisibility(boolean isAdded) {
        NavigationKind kind = NavigationKind.edit_tabs; // タブを変更した時は必ずこのタブにいる
        this.kind = kind;
        areTabsBeingModified = true;

        if (pager.getAdapter() != null) {
            MainPagerAdapter adapter = (MainPagerAdapter) pager.getAdapter();
            adapter.removeFragments(getChildFragmentManager(), pager);
            adapter = new MainPagerAdapter(getChildFragmentManager(), listener.getVisibleKinds(), kind);
            pager.setAdapter(adapter);
            adapter.notifyDataSetChanged(); // これを呼ばないと、古いpositionでonTabSelectedが呼ばれてしまう
        }

        TabLayout tabs = listener.getTabLayout();
        refreshTabTitles(tabs);

        areTabsBeingModified = false;
        TabLayout.Tab tab = tabs.getTabAt(listener.getVisibleKinds().indexOf(kind));
        if (tab != null) {
            tab.select(); // EditTabsを選択状態にする
        }
        listener.requestRefreshUi(kind);
    }

    public interface OnFragmentInteractionListener {
        void requestRefreshUi(NavigationKind kind);

        List<NavigationKind> getVisibleKinds();

        TabLayout getTabLayout();
    }
}
