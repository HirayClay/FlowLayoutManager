package com.lm;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hiray on 2018/1/27.
 *
 * @author hiray
 */

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    List<Row> rows = new ArrayList<>();

    public FlowLayoutManager() {
        setAutoMeasureEnabled(true);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 && state.isPreLayout())
            return;
        updateAnchorInfo(recycler, state);
        detachAndScrapAttachedViews(recycler);
//        if (state.isPreLayout()) {
        fill(recycler, state);
//        }
    }

    private void fill(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int width = getWidth();
        int height = getHeight();
        int remainingSpace = width;
        int viewWidth;
        int start = 0;
        Row row = new Row(this);
        for (int i = 0; i < state.getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            measureChild(view, 0, 0);
            addView(view);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            viewWidth = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            if (viewWidth <= remainingSpace && height > 0) {
                remainingSpace -= viewWidth;
                row.addView(view);
            } else if (viewWidth > remainingSpace) {
                height -= row.getRowHeight();
                row.setCoordinate(start);
                row.layout();
                row = new Row(this);
                row.addView(view);
                remainingSpace = width;
                start += row.getRowHeight();
            }
        }
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return super.scrollHorizontallyBy(dx, recycler, state);
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    private void updateAnchorInfo(RecyclerView.Recycler recycler, RecyclerView.State state) {
        //rows is empty,wo need build from scratch
        if (rows.isEmpty())
            return;

        int childCount = getChildCount();
        View lessMatch;
        View leastMatch;
        View match;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
        }

    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    class AnchorRow {
        int coordinate;
        int postion;
    }
}