package com.coinkarasu.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.CoinActivity;
import com.coinkarasu.activities.MainActivity;
import com.coinkarasu.activities.MainFragment;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.adapters.row.CoinListViewHolder;
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
    public static void showTabLayoutTutorial(final Activity activity, final TabLayout targetView) {
        if (activity == null || activity.isFinishing() || AppTutorialChecker.hasStarted(activity, ID_TAB_LAYOUT)) {
            return;
        }

        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<NavigationKind> visibleKinds = ((MainActivity) activity).getVisibleKinds();
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
                            .setDismissText(R.string.tutorial_dismiss)
                            .setContentText(R.string.tutorial_content_tablayout)
                            .withRectangleShape()
                            .setShapePadding(PADDING_RECT)
                            .build());

                    View tabView = ((ViewGroup) targetView.getChildAt(0)).getChildAt(index);

                    sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                            .setTarget(tabView)
                            .setDismissText("")
                            .setContentText(R.string.tutorial_content_tablayout_tap_tab)
                            .setTargetTouchable(true)
                            .setListener(new IShowcaseListener() {
                                @Override
                                public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                                }

                                @Override
                                public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                                    if (!activity.isFinishing()) {
                                        MainFragment fragment = ((MainActivity) activity).getFragment();
                                        fragment.setCurrentKind(NavigationKind.coincheck, true);
                                        logTutorialProgress(((MainActivity) activity).getFirebaseAnalytics(), ID_TAB_LAYOUT);
                                    }
                                }
                            })
                            .build());

                    sequence.start();
                }
            }, DELAY);
        } catch (Exception e) {
            CKLog.e(TAG, e);
        } finally {
            AppTutorialChecker.onTutorialFinished(activity, ID_TAB_LAYOUT);
        }
    }

    // MainActivityのタブのチュートリアル。今はCoincheckタブを使っている
    public static void showTabTutorial(final Activity activity, final RecyclerView recyclerView) {
        if (activity == null || activity.isFinishing() || AppTutorialChecker.hasStarted(activity, ID_TAB)) {
            return;
        }

        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ShowcaseConfig config = new ShowcaseConfig();
                    config.setDelay(DELAY);

                    MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, String.valueOf(System.currentTimeMillis()));
                    sequence.setConfig(config);

                    final View rowView = ((CoinListViewHolder) recyclerView.findViewHolderForAdapterPosition(1)).container;

                    sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                            .setTarget(recyclerView)
                            .setDismissText(R.string.tutorial_dismiss)
                            .setContentText(R.string.tutorial_content_tab)
                            .withoutShape()
                            .build());

                    sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                            .setTarget(rowView)
                            .setDismissText(R.string.tutorial_dismiss)
                            .setContentText(R.string.tutorial_content_coin)
                            .withRectangleShape()
                            .setShapePadding(PADDING_RECT)
                            .build());

                    sequence.addSequenceItem(new MaterialShowcaseView.Builder(activity)
                            .setTarget(rowView)
                            .setDismissText("")
                            .setContentText(R.string.tutorial_content_coin_tap_coin)
                            .setTargetTouchable(true)
                            .setListener(new IShowcaseListener() {
                                @Override
                                public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {
                                }

                                @Override
                                public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                                    logTutorialProgress(((MainActivity) activity).getFirebaseAnalytics(), ID_TAB);
                                }
                            })
                            .withRectangleShape()
                            .setShapePadding(PADDING_RECT)
                            .build());

                    sequence.start();
                }
            }, DELAY);
        } catch (Exception e) {
            CKLog.e(TAG, e);
        } finally {
            AppTutorialChecker.onTutorialFinished(activity, ID_TAB);
        }
    }

    // CoinActivityのPriceOverviewチュートリアル
    public static void showPriceOverviewTutorial(final Activity activity, View targetView) {
        if (activity == null || activity.isFinishing() || AppTutorialChecker.hasStarted(activity, ID_PRICE_OVERVIEW)) {
            return;
        }

        try {
            build(activity, targetView, R.string.tutorial_dismiss, R.string.tutorial_content_price_overview, true, new Runnable() {
                @Override
                public void run() {
                    if (!activity.isFinishing()) {
                        ScrollView scroll = activity.findViewById(R.id.scroll_view);
                        View card = activity.findViewById(R.id.historical_price);
                        scroll.scrollTo(0, card.getBottom());

                        logTutorialProgress(((CoinActivity) activity).getFirebaseAnalytics(), ID_PRICE_OVERVIEW);
                        logTutorialComplete(((CoinActivity) activity).getFirebaseAnalytics());
                    }
                }
            }).start();
        } catch (Exception e) {
            CKLog.e(TAG, e);
        } finally {
            AppTutorialChecker.onTutorialFinished(activity, ID_PRICE_OVERVIEW);
        }
    }

    private static MaterialShowcaseSequence build(final Activity activity, View target,
                                                  int dismissTextResId, int contentTextResId, boolean isRect, final Runnable executeOnDismiss) {
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(DELAY);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, String.valueOf(System.currentTimeMillis()));
        sequence.setConfig(config);

        MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(activity)
                .setTarget(target)
                .setDismissText(dismissTextResId)
                .setContentText(contentTextResId)
                .setTargetTouchable(true)
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
