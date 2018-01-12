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
    private TabLayout.Tab tab;

    public MainFragment() {
    }

    public static MainFragment newInstance(NavigationKind kind) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString("kind", kind.name());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            kind = NavigationKind.valueOf(getArguments().getString("kind"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        MainPagerAdapter adapter = new MainPagerAdapter(getChildFragmentManager(), getActivity(), kind);

        if (savedInstanceState != null) {
            kind = NavigationKind.valueOf(savedInstanceState.getString(STATE_SELECTED_KIND_KEY));
            if (!kind.isVisible(getActivity())) {
                kind = NavigationKind.home;
            }
            adapter.setVersion(savedInstanceState.getLong(STATE_PAGER_ADAPTER_VERSION));
            if (DEBUG) CKLog.d(TAG, "Set version to PagerAdapter " + adapter.getVersion());
        }

        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(NavigationKind.values().length);

        TabLayout tabs = getActivity().findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);
        tabs.addOnTabSelectedListener(this);
        refreshTabTitles(tabs);

        int position = NavigationKind.visiblePosition(getActivity(), kind);
        tab = tabs.getTabAt(position);
        pager.setCurrentItem(position, false); // 他のアクティビティから戻ってきた時のみ、onTabSelectedが呼ばれる
        listener.onPageChanged(kind);

        return view;
    }

    public void refreshTabTitles(TabLayout tabs) {
        if (tabs == null) {
            return;
        }

        List<NavigationKind> values = NavigationKind.visibleValues(getActivity());
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

    public void setCurrentKind(NavigationKind kind, boolean smoothScroll) {
        if (getView() == null) {
            return;
        }
        this.kind = kind;
        int position = NavigationKind.visiblePosition(getActivity(), kind);
        ((ViewPager) getView().findViewById(R.id.view_pager)).setCurrentItem(position, smoothScroll);
        listener.onPageChanged(kind);
    }

    @Override
    public void onResume() {
        super.onResume();
        listener.onPageChanged(kind);
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
        kind = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SELECTED_KIND_KEY, kind.name());
        if (getView() != null) {
            PagerAdapter adapter = ((ViewPager) getView().findViewById(R.id.view_pager)).getAdapter();
            if (adapter != null) {
                savedInstanceState.putLong(STATE_PAGER_ADAPTER_VERSION, ((MainPagerAdapter) adapter).getVersion());
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * 以前はonPageScrollStateChangedを使っていたが、TabLayout.Tabが渡される分こっちが便利なので変えた。
     * スクロールした時とタブをクリックしたときの両方で、onTabSelectedの方が先に呼ばれる。
     */
    @Override
    public void onTabSelected(TabLayout.Tab _tab) {
        if (DEBUG) CKLog.d(TAG, "onTabSelected " + _tab.getPosition()
                + " ignore=" + (tab == null));
        if (tab == null || getView() == null) {
            return;
        }

        tab = _tab;
        int position = _tab.getPosition();
        ((ViewPager) getView().findViewById(R.id.view_pager)).setCurrentItem(position, false);
        kind = NavigationKind.visibleValues(getActivity()).get(position);
        listener.onPageChanged(kind);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    public void refreshTabVisibility(boolean isAdded) {
        kind = NavigationKind.edit_tabs;
        ViewPager pager = getView().findViewById(R.id.view_pager);
        tab = null;

        if (pager.getAdapter() != null) {
            MainPagerAdapter adapter = (MainPagerAdapter) pager.getAdapter();
            adapter.removeFragments(getChildFragmentManager(), pager);
            adapter = new MainPagerAdapter(getChildFragmentManager(), getActivity(), kind);
            pager.setAdapter(adapter);
            adapter.notifyDataSetChanged(); // これを呼ばないと、古いpositionでonTabSelectedが呼ばれてしまう
        }

        int position = NavigationKind.visiblePosition(getActivity(), kind);
        TabLayout tabs = getActivity().findViewById(R.id.tab_layout);
        tab = tabs.getTabAt(position);
        refreshTabTitles(tabs);

        pager.setCurrentItem(position, false);
        tab.select(); // EditTabsを選択状態にする
        listener.onPageChanged(kind);
    }

    public interface OnFragmentInteractionListener {
        void onPageChanged(NavigationKind kind);
    }
}
