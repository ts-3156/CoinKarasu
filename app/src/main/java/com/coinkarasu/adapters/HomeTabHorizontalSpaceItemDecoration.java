package com.coinkarasu.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HomeTabHorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int horizontalGap;

    public HomeTabHorizontalSpaceItemDecoration(Context context, int horizontalGap) {
        this.horizontalGap = horizontalGap;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int size = parent.getAdapter().getItemCount();

        if (position == 0) {
            outRect.left = horizontalGap;
        }

        outRect.right = horizontalGap;
    }
}