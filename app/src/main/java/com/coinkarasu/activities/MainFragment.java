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
import com.coinkarasu.utils.Log;

import java.util.ArrayList;


public class MainFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    private static final boolean DEBUG = true;
    private static final String TAG = "MainFragment";
    private static final String STATE_SELECTED_KIND_KEY = "kind";
    private static final String STATE_PAGER_ADAPTER_VERSION = "pagerAdapterVersion";

    private OnFragmentInteractionListener listener;

    private NavigationKind kind;
    private TabLayout.Tab tab;
    private Log logger;

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
        logger = new Log(getActivity());

        MainPagerAdapter adapter = new MainPagerAdapter(getChildFragmentManager(), getActivity(), kind);

        if (savedInstanceState != null) {
            kind = NavigationKind.valueOf(savedInstanceState.getString(STATE_SELECTED_KIND_KEY));
            if (!kind.isVisible(getActivity())) {
                kind = NavigationKind.home;
            }
            adapter.setVersion(savedInstanceState.getLong(STATE_PAGER_ADAPTER_VERSION));
            if (DEBUG) logger.d(TAG, "Set version to PagerAdapter " + adapter.getVersion());
        }

        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(adapter);
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(NavigationKind.visibleValues(getActivity()).size());

        TabLayout tabs = getActivity().findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);
        updateTabTitles(tabs);

        int position = NavigationKind.visiblePosition(getActivity(), kind);
        tab = tabs.getTabAt(position);
        pager.setCurrentItem(position); // #setCurrentItem doesn't call #onPageScrollStateChanged.
        listener.onPageChanged(kind);

        return view;
    }

    public void updateTabTitles(TabLayout tabs) {
        if (tabs == null) {
            return;
        }

        ArrayList<NavigationKind> values = NavigationKind.visibleValues(getActivity());
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

    public void setCurrentKind(NavigationKind kind) {
        if (getView() == null) {
            return;
        }
        this.kind = kind;
        int position = NavigationKind.visiblePosition(getActivity(), kind);
        ((ViewPager) getView().findViewById(R.id.view_pager)).setCurrentItem(position);
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            if (getView() == null || getActivity() == null) {
                return;
            }

            int position = ((ViewPager) getView().findViewById(R.id.view_pager)).getCurrentItem();

            if (position != tab.getPosition()) {
                kind = NavigationKind.visibleValues(getActivity()).get(position);
                tab = ((TabLayout) getActivity().findViewById(R.id.tab_layout)).getTabAt(position);
                listener.onPageChanged(kind);
            }
        }
    }

    public void updateTabVisibility(boolean isAdded) {
        ViewPager pager = getView().findViewById(R.id.view_pager);
        if (pager.getAdapter() != null) {
            ((MainPagerAdapter) pager.getAdapter()).notifyTabChanged(kind, isAdded);
        }

        TabLayout tabs = getActivity().findViewById(R.id.tab_layout);
        updateTabTitles(tabs);

        int position = NavigationKind.visiblePosition(getActivity(), kind);
        pager.setCurrentItem(position, false);
        tabs.getTabAt(position).select();
        listener.onPageChanged(kind);
    }

    public interface OnFragmentInteractionListener {
        void onPageChanged(NavigationKind kind);
    }
}
