package com.coinkarasu.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.CoinActivity;
import com.coinkarasu.activities.MainActivity;
import com.coinkarasu.activities.MainFragment;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.CoinListAdapter;
import com.coinkarasu.adapters.row.CoinListViewHolder;
import com.coinkarasu.coins.Coin;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class Tutorial {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "Tutorial";

    private static final int PADDING_RECT = 48;
    private static final int DELAY = 1000;
    private static final String ID_SUFFIX = BuildConfig.DEBUG ? "_debug041" : "";
    private static final String ID_TAB_LAYOUT = "tutorial_tab_layout" + ID_SUFFIX;
    private static final String ID_TAB = "tutorial_tab" + ID_SUFFIX;
    private static final String ID_PRICE_OVERVIEW = "tutorial_price_overview" + ID_SUFFIX;

    // MainActivityのTabLayoutのチュートリアル
    public static void showTabLayoutTutorial(final MainActivity activity) {
        if (activity == null || activity.isFinishing() || AppTutorialChecker.hasStarted(activity, ID_TAB_LAYOUT)) {
            return;
        }

        try {
            final TabLayout targetView = activity.getTabLayout();

            List<NavigationKind> visibleKinds = activity.getVisibleKinds();
            int index = visibleKinds.indexOf(NavigationKind.coincheck);
            if (index < 0) {
                if (DEBUG) CKLog.w(TAG, "showTabLayoutTutorial() Cancel since specified tab is not visible");
                return;
            }

            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(DELAY);

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, String.valueOf(System.currentTimeMillis()));
            sequence.setConfig(config);

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                    .setTarget(targetView)
                    .setDismissOnTouch(true)
                    .setDismissText(R.string.tutorial_dismiss)
                    .setContentText(R.string.tutorial_content_tablayout)
                    .withRectangleShape()
                    .setShapePadding(PADDING_RECT)
                    .build());

            View tabView = ((ViewGroup) targetView.getChildAt(0)).getChildAt(index);

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                    .setTarget(tabView)
                    .setDismissOnTouch(true)
                    .setDismissText(R.string.tutorial_dismiss)
                    .setContentText(R.string.tutorial_content_tablayout_tap_tab)
                    .setListener(new IShowcaseListener() {
                        @Override
                        public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                        }

                        @Override
                        public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                            if (!activity.isFinishing()) {
                                MainFragment fragment = activity.getFragment();
                                fragment.setCurrentKind(NavigationKind.coincheck, true);
                                logTutorialProgress(activity.getFirebaseAnalytics(), ID_TAB_LAYOUT);
                            }
                        }
                    })
                    .build());

            sequence.start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        } finally {
            AppTutorialChecker.onTutorialFinished(activity, ID_TAB_LAYOUT);
        }
    }

    // MainActivityのタブのチュートリアル
    public synchronized static void showTabTutorial(final MainActivity activity, RecyclerView recyclerView, NavigationKind kind) {
        if (activity == null || activity.isFinishing() || AppTutorialChecker.hasStarted(activity, ID_TAB)) {
            return;
        }

        try {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(DELAY);

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, String.valueOf(System.currentTimeMillis()));
            sequence.setConfig(config);

            View rowView = ((CoinListViewHolder) recyclerView.findViewHolderForAdapterPosition(1)).container;
            Coin coin = ((CoinListAdapter) recyclerView.getAdapter()).getItem(1);
            int padding = activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
            String tabName = activity.getString(kind.tabStrResId);

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                    .setTarget(rowView)
                    .setDismissOnTouch(true)
                    .setMaskColour(0x00ffffff)
                    .setDismissText(R.string.tutorial_dismiss)
                    .setDismissTextBackgroundColor(0xdd335075)
                    .setDismissTextPaddding(padding, padding, padding, padding)
                    .setContentText(activity.getString(R.string.tutorial_content_tab, tabName))
                    .setContentTextBackgroundColor(0xdd335075)
                    .setContentTextPaddding(padding, padding, padding, padding)
                    .withoutShape()
                    .build());

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                    .setTarget(rowView)
                    .setDismissOnTouch(true)
                    .setDismissText(R.string.tutorial_dismiss)
                    .setContentText(activity.getString(R.string.tutorial_content_coin, coin.getSymbol()))
                    .withRectangleShape()
                    .setShapePadding(PADDING_RECT)
                    .build());

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                    .setTarget(rowView)
                    .setDismissOnTouch(true)
                    .setDismissText(R.string.tutorial_dismiss)
                    .setContentText(R.string.tutorial_content_coin_tap_coin)
                    .setTargetTouchable(true)
                    .setListener(new IShowcaseListener() {
                        @Override
                        public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                        }

                        @Override
                        public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                            logTutorialProgress(activity.getFirebaseAnalytics(), ID_TAB);
                        }
                    })
                    .withRectangleShape()
                    .setShapePadding(PADDING_RECT)
                    .build());

            sequence.start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        } finally {
            AppTutorialChecker.onTutorialFinished(activity, ID_TAB);
        }
    }

    // CoinActivityのPriceOverviewチュートリアル
    public static void showPriceOverviewTutorial(final CoinActivity activity, View targetView, Coin coin) {
        if (activity == null || activity.isFinishing() || AppTutorialChecker.hasStarted(activity, ID_PRICE_OVERVIEW)) {
            return;
        }

        try {
            ShowcaseConfig config = new ShowcaseConfig();
            config.setDelay(DELAY);

            MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, String.valueOf(System.currentTimeMillis()));
            sequence.setConfig(config);

            sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                    .setTarget(targetView)
                    .setDismissOnTouch(true)
                    .setDismissText(R.string.tutorial_finish)
                    .setContentText(activity.getString(R.string.tutorial_content_price_overview, coin.getSymbol()))
                    .setListener(new IShowcaseListener() {
                        @Override
                        public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                        }

                        @Override
                        public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                            logTutorialProgress(activity.getFirebaseAnalytics(), ID_PRICE_OVERVIEW);
                            logTutorialComplete(activity.getFirebaseAnalytics());
                        }
                    })
                    .withRectangleShape()
                    .setShapePadding(PADDING_RECT)
                    .build());

            sequence.start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        } finally {
            AppTutorialChecker.onTutorialFinished(activity, ID_PRICE_OVERVIEW);
        }
    }

    public static void reset(Context context) {
        AppTutorialChecker.reset(context, ID_TAB_LAYOUT);
        AppTutorialChecker.reset(context, ID_TAB);
        AppTutorialChecker.reset(context, ID_PRICE_OVERVIEW);
    }

    public static void logTutorialBegin(FirebaseAnalytics analytics) {
        if (analytics != null) {
            Bundle bundle = new Bundle();
            analytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, bundle);
        }
    }

    private static void logTutorialProgress(FirebaseAnalytics analytics, String id) {
        if (analytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("tutorial_id", id);
            analytics.logEvent("tutorial_progress", bundle);
        }
    }

    private static void logTutorialComplete(FirebaseAnalytics analytics) {
        if (analytics != null) {
            Bundle bundle = new Bundle();
            analytics.logEvent(FirebaseAnalytics.Event.TUTORIAL_COMPLETE, bundle);
        }
    }
}
