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


public class MainFragment extends Fragment implements
        ViewPager.OnPageChangeListener {

    public enum NavigationKind {
        home(R.string.nav_home, R.string.tab_home, R.id.nav_home, R.color.colorNavHome, R.color.colorNavHomeDark, -1, new String[]{}, 0),
        japan(R.string.nav_japan, R.string.tab_japan, R.id.nav_japan, R.color.colorNavJapan, R.color.colorNavJapanDark, R.array.japan_all_symbols, new String[]{"bitflyer", "coincheck", "zaif"}, 1),
        jpy_toplist(R.string.nav_jpy_toplist, R.string.tab_jpy_toplist, R.id.nav_jpy_toplist, R.color.colorJpyToplist, R.color.colorJpyToplistDark, R.array.jpy_toplist_symbols, new String[]{"cccagg"}, 2),
        usd_toplist(R.string.nav_usd_toplist, R.string.tab_usd_toplist, R.id.nav_usd_toplist, R.color.colorUsdToplist, R.color.colorUsdToplistDark, R.array.usd_toplist_symbols, new String[]{"cccagg"}, 3),
        eur_toplist(R.string.nav_eur_toplist, R.string.tab_eur_toplist, R.id.nav_eur_toplist, R.color.colorEurToplist, R.color.colorEurToplistDark, R.array.eur_toplist_symbols, new String[]{"cccagg"}, 4),
        btc_toplist(R.string.nav_btc_toplist, R.string.tab_btc_toplist, R.id.nav_btc_toplist, R.color.colorBtcToplist, R.color.colorBtcToplistDark, R.array.btc_toplist_symbols, new String[]{"cccagg"}, 5);

        int navStrResId;
        int tabStrResId;
        int navResId;
        int colorResId;
        int colorDarkResId;
        public int symbolsResId;
        String[] exchanges;
        int navPos;

        NavigationKind(int navStrResId, int tabStrResId, int navResId, int colorResId, int colorDarkResId, int symbolsResId, String[] exchanges, int navPos) {
            this.navStrResId = navStrResId;
            this.tabStrResId = tabStrResId;
            this.navResId = navResId;
            this.colorResId = colorResId;
            this.colorDarkResId = colorDarkResId;
            this.symbolsResId = symbolsResId;
            this.exchanges = exchanges;
            this.navPos = navPos;
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
        }

        ViewPager pager = view.findViewById(R.id.view_pager);
        pager.setAdapter(new MainPagerAdapter(getChildFragmentManager(), kind));
        pager.addOnPageChangeListener(this);
        pager.setOffscreenPageLimit(NavigationKind.values().length);

        TabLayout tabs = getActivity().findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(pager);
        tabs.getTabAt(NavigationKind.japan.ordinal()).setText(NavigationKind.japan.tabStrResId);
        tabs.getTabAt(NavigationKind.home.ordinal()).setText(NavigationKind.home.tabStrResId);
        tabs.getTabAt(NavigationKind.jpy_toplist.ordinal()).setText(NavigationKind.jpy_toplist.tabStrResId);
        tabs.getTabAt(NavigationKind.usd_toplist.ordinal()).setText(NavigationKind.usd_toplist.tabStrResId);
        tabs.getTabAt(NavigationKind.eur_toplist.ordinal()).setText(NavigationKind.eur_toplist.tabStrResId);
        tabs.getTabAt(NavigationKind.btc_toplist.ordinal()).setText(NavigationKind.btc_toplist.tabStrResId);

        tab = tabs.getTabAt(kind.ordinal());
        pager.setCurrentItem(kind.ordinal()); // #setCurrentItem doesn't call #onPageScrollStateChanged.
        listener.onPageChanged(kind);

        return view;
    }

    public NavigationKind getCurrentKind() {
        return kind;
    }

    public void setCurrentKind(NavigationKind kind) {
        if (getView() == null) {
            return;
        }
        this.kind = kind;
        ((ViewPager) getView().findViewById(R.id.view_pager)).setCurrentItem(kind.ordinal());
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
                kind = NavigationKind.values()[position];
                tab = ((TabLayout) getActivity().findViewById(R.id.tab_layout)).getTabAt(kind.ordinal());
                listener.onPageChanged(kind);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onPageChanged(NavigationKind kind);
    }
}
