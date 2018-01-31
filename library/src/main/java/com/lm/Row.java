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
        for (View v :
                views) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
            int vh = v.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (vh > rowHeight)
                rowHeight = vh;
        }
        return rowHeight;
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
        if (rowHeight == NaN)
            getRowHeight();
        int size = views.size();
        int left = 0, top, right, bottom;
        for (int i = 0; i < size; i++) {
            View view = views.get(i);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            layoutManager.addView(view);
            left += lp.leftMargin;
            right = left + view.getMeasuredWidth() + lp.rightMargin;
            top = rowHeight / 2 - view.getMeasuredHeight() / 2 + coordinate;
            bottom = top + view.getMeasuredHeight();
            layoutManager.layoutDecorated(view, left, top, right, bottom);
            left = right;
        }
    }

    public void setCoordinate(int coordinate) {
        this.coordinate = coordinate;
    }

    public boolean willBeOut(int offset) {
        return coordinate + getRowHeight() - offset <= 0 || coordinate + offset >= layoutManager.getHeight();
    }

    /**
     * 发生滑动之后，更新基准坐标
     *
     * @param delta applied to coordinate
     */
    public void updateCooordinate(int delta) {
        coordinate += delta;
    }
}
