package com.coinkarasu.utils;

import android.app.Activity;
import android.support.v4.app.AppLaunchChecker;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ScrollView;

import com.coinkarasu.R;
import com.coinkarasu.activities.CoinActivity;
import com.coinkarasu.activities.MainActivity;
import com.coinkarasu.activities.MainFragment;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.row.CoinListViewHolder;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class Tutorial {
    private static final boolean DEBUG = true;
    private static final String TAG = "Tutorial";

    private static final int PADDING_RECT = 48;
    private static final String ID_TAB_LAYOUT = "tab_layout_007";
    private static final String ID_TAB = "tab_007";
    private static final String ID_COIN = "coin_007";
    private static final String ID_PRICE_OVERVIEW = "historical_price_card_007";

    // MainActivityのTabLayoutのチュートリアル
    public static void showTabLayoutTutorial(final Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (AppLaunchChecker.hasStartedFromLauncher(activity)) {
            return;
        }

        try {
            build(ID_TAB_LAYOUT, activity, activity.findViewById(R.id.tab_layout),
                    R.string.tutorial_dismiss, R.string.tutorial_content_tablayout, true, new Runnable() {
                        @Override
                        public void run() {
                            if (!activity.isFinishing()) {
                                MainFragment fragment = ((MainActivity) activity).getFragment();
                                fragment.setCurrentKind(NavigationKind.coincheck, true);
                            }
                        }
                    }).start();

            AppLaunchChecker.onActivityCreate(activity);
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    // MainActivityのタブのチュートリアル。今はCoincheckタブを使っている
    public static void showTabTutorial(final Activity activity, final RecyclerView view) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        try {
            build(ID_TAB, activity, activity.findViewById(R.id.tab_layout),
                    R.string.tutorial_dismiss, R.string.tutorial_content_tab, false, new Runnable() {
                        @Override
                        public void run() {
                            showCoinTutorial(activity, view);
                        }
                    }).start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    // MainActivityのコインのチュートリアル
    public static void showCoinTutorial(final Activity activity, final RecyclerView view) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        CoinListViewHolder holder = (CoinListViewHolder) view.findViewHolderForAdapterPosition(1);

        try {
            build(ID_COIN, activity, holder.container, R.string.tutorial_dismiss, R.string.tutorial_content_coin, true, new Runnable() {
                @Override
                public void run() {
                    if (!activity.isFinishing()) {
                        CoinListAdapter adapter = (CoinListAdapter) view.getAdapter();
                        if (adapter != null) {
                            CoinActivity.start(activity, adapter.getItem(1), NavigationKind.coincheck, false);
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    // CoinActivityのPriceOverviewチュートリアル
    public static void showPriceOverviewTutorial(final Activity activity, View view) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        try {
            build(ID_PRICE_OVERVIEW, activity, view.findViewById(R.id.container), R.string.tutorial_dismiss, R.string.tutorial_content_price_overview, true, new Runnable() {
                @Override
                public void run() {
                    if (!activity.isFinishing()) {
                        ScrollView scroll = activity.findViewById(R.id.scroll_view);
                        View card = activity.findViewById(R.id.card_line_chart);
                        scroll.scrollTo(0, card.getBottom());
                    }
                }
            }).start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        }
    }

    private static MaterialShowcaseSequence build(String id, final Activity activity, View target,
                                                  int dismissTextResId, int contentTextResId, boolean isRect, final Runnable executeOnDismiss) {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(1000);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, id);
        sequence.setConfig(config);

        MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(activity)
                .setTarget(target)
                .setDismissText(dismissTextResId)
                .setContentText(contentTextResId)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                        if (executeOnDismiss != null) {
                            executeOnDismiss.run();
                        }
                    }
                });

        if (isRect) {
            builder.withRectangleShape().setShapePadding(PADDING_RECT);
        }

        sequence.addSequenceItem(builder.build());

        if (sequence.hasFired()) {
        }

        return sequence;
    }
}
