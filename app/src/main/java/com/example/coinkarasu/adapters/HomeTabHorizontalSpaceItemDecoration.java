package com.example.coinkarasu.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

public class HomeTabHorizontalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int spaceWidth;

    public HomeTabHorizontalSpaceItemDecoration(Context context, int spaceWidth) {
        this.spaceWidth = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, spaceWidth, context.getResources().getDisplayMetrics());
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int size = parent.getAdapter().getItemCount();

        if (position == 0) {
            outRect.left = spaceWidth;
        }

        outRect.right = spaceWidth;
    }
}