package com.coinkarasu.adapters;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HomeTabVerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int verticalGap;
    private int spanCount;

    public HomeTabVerticalSpaceItemDecoration(int verticalGap, int spanCount) {
        this.verticalGap = verticalGap;
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int size = parent.getAdapter().getItemCount();

        if (position < size - size % spanCount) {
            outRect.bottom = verticalGap;
        }
    }
}
