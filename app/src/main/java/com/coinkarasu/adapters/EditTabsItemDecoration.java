package com.coinkarasu.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class EditTabsItemDecoration extends RecyclerView.ItemDecoration {

    private int verticalGap;

    private static final int[] ATTRS = {android.R.attr.listDivider};

    private Drawable divider;

    public EditTabsItemDecoration(Context context) {
        this(context, 0);
    }

    public EditTabsItemDecoration(Context context, int verticalGap) {
        TypedArray ta = context.obtainStyledAttributes(ATTRS);
        divider = ta.getDrawable(0);
        ta.recycle();

        this.verticalGap = verticalGap;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            // Border between items
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);

            // Border on top
            if (i == 0 || i == 1) {
                top = child.getTop() - params.topMargin;
                bottom = top + divider.getIntrinsicHeight();
                divider.setBounds(left, top, right, bottom);
                divider.draw(canvas);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int size = parent.getAdapter().getItemCount();

        if (position == 0) {
            outRect.bottom = verticalGap;
        }
    }
}