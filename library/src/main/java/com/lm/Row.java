package com.lm;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hiray on 2018/1/27.
 *
 * @author hiray
 *         save info of a row of views
 */

public class Row {

    static final int NaN = Integer.MIN_VALUE;
    private List<View> views = new ArrayList<>();

    private int coordinate = NaN;
    private int rowHeight = NaN;
    private RecyclerView.LayoutManager layoutManager;

    public Row(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    public int getRowHeight() {
        if (NaN != rowHeight)
            return rowHeight;
        int h = 0;
        for (View v :
                views) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
            int vh = v.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (vh > h)
                h = vh;
        }
        rowHeight = h;
        return h;
    }

    public void addView(View itemView) {
        views.add(itemView);
    }

    public void clear() {
        views.clear();
        rowHeight = coordinate = NaN;
    }

    public boolean has(View view) {
        return views.contains(view);
    }

    /**
     * perform layout for views in this row
     */
    public void layout() {
        int size = views.size();
        int left = 0, top, right, bottom;
        for (int i = 0; i < size; i++) {
            View view = views.get(i);
            layoutManager.addView(view);
            right = left + view.getMeasuredWidth();
            top = rowHeight / 2 - view.getMeasuredHeight() / 2 + coordinate;
            bottom = top + view.getMeasuredHeight();
            layoutManager.layoutDecoratedWithMargins(view, left, top, right, bottom);
            left += view.getMeasuredWidth();
        }
    }

    public void setCoordinate(int coordinate) {
        this.coordinate = coordinate;
    }
}
