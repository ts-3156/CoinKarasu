package com.example.coinkarasu.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.coinkarasu.R;
import com.example.coinkarasu.pagers.MainPagerAdapter;
import com.example.coinkarasu.utils.PrefHelper;

import java.util.ArrayList;


public class MainFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    public enum NavigationKind {
        home(R.string.nav_home, R.string.tab_home, R.id.nav_home, R.color.colorNavHome, R.color.colorNavHomeDark, R.color.state_nav_home, -1, new String[]{}),
        japan(R.string.nav_japan, R.string.tab_japan, R.id.nav_japan, R.color.colorNavJapan, R.color.colorNavJapanDark, R.color.state_nav_japan, R.array.japan_all_symbols, new String[]{"bitflyer", "coincheck", "zaif"}),
        jpy_toplist(R.string.nav_jpy_toplist, R.string.tab_jpy_toplist, R.id.nav_jpy_toplist, R.color.colorJpyToplist, R.color.colorJpyToplistDark, R.color.state_nav_jpy_toplist, R.array.jpy_toplist_symbols, new String[]{"cccagg"}),
        usd_toplist(R.string.nav_usd_toplist, R.string.tab_usd_toplist, R.id.nav_usd_toplist, R.color.colorUsdToplist, R.color.colorUsdToplistDark, R.color.state_nav_usd_toplist, R.array.usd_toplist_symbols, new String[]{"cccagg"}),
        eur_toplist(R.string.nav_eur_toplist, R.string.tab_eur_toplist, R.id.nav_eur_toplist, R.color.colorEurToplist, R.color.colorEurToplistDark, R.color.state_nav_eur_toplist, R.array.eur_toplist_symbols, new String[]{"cccagg"}),
        btc_toplist(R.string.nav_btc_toplist, R.string.tab_btc_toplist, R.id.nav_btc_toplist, R.color.colorBtcToplist, R.color.colorBtcToplistDark, R.color.state_nav_btc_toplist, R.array.btc_toplist_symbols, new String[]{"cccagg"}),
        edit_tabs(R.string.nav_edit_tabs, R.string.tab_edit_tabs, R.id.nav_edit_tabs, R.color.colorEditTabs, R.color.colorEditTabsDark, R.color.state_nav_edit_tabs, -1, new String[]{});

        int navStrResId;
        public int tabStrResId;
        int navResId;
        int colorResId;
        int colorDarkResId;
        int colorStateResId;
        public int symbolsResId;
        String[] exchanges;

        NavigationKind(int navStrResId, int tabStrResId, int navResId, int colorResId, int colorDarkResId, int colorStateResId, int symbolsResId, String[] exchanges) {
            this.navStrResId = navStrResId;
            this.tabStrResId = tabStrResId;
            this.navResId = navResId;
            this.colorResId = colorResId;
            this.colorDarkResId = colorDarkResId;
            this.colorStateResId = colorStateResId;
            this.symbolsResId = symbolsResId;
            this.exchanges = exchanges;
        }

        public boolean isHideable() {
            return this != home && this != edit_tabs;
        }

        public boolean isVisible(Context context) {
            return !isHideable() || PrefHelper.isVisibleTab(context, this);
        }

        public static ArrayList<NavigationKind> visibleValues(Context context) {
            ArrayList<NavigationKind> values = new ArrayList<>();
            for (NavigationKind kind : values()) {
                if (kind.isVisible(context)) {
                    values.add(kind);
                }
            }
            return values;
        }

        public static int visiblePosition(Context context, NavigationKind kind) {
            return visibleValues(context).indexOf(kind);
        }

        public boolean defaultVisibility() {
            return this != eur_toplist && this != btc_toplist;
        }

        static NavigationKind valueByNavResId(int navResId) {
            for (NavigationKind kind : values()) {
                if (kind.navResId == navResId) {
                    return kind;
                }
            }
            return null;
        }
    }

    private static final String STATE_SELECTED_KIND_KEY = "kind";

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

        if (savedInstanceState != null) {
            kind = NavigationKind.valueOf(savedInstanceState.getString(STATE_SELECTED_KIND_KEY));
            if (!kind.isVisible(getActivity())) {
                kind = NavigationKind.home;
            }
        }

        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new MainPagerAdapter(getChildFragmentManager(), getActivity(), kind));
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

    public void updateTabVisibility() {
        ViewPager pager = getView().findViewById(R.id.view_pager);
        pager.getAdapter().notifyDataSetChanged();

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
