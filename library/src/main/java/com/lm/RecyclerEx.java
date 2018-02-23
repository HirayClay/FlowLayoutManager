package com.lm;

import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by hiray on 2018/1/29.
 *
 * @author hiray
 */

public class RecyclerEx {
    private final RecyclerView.LayoutManager layoutManager;
    private final RecyclerView.State state;
    private RecyclerView.Recycler recycler;
    ArrayMap<Integer, Row> rowMap = new ArrayMap<>();

    public RecyclerEx(RecyclerView.Recycler recycler, RecyclerView.LayoutManager layoutManager,
                      RecyclerView.State state) {
        this.recycler = recycler;
        this.layoutManager = layoutManager;
        this.state = state;
    }

    //获取对应行的Row
    //如果没有则找出已有row中最大的View 往后继续创建一行
    public Row getRowForPosition(int position) {
        if (rowMap.containsKey(position)) {
            return rowMap.get(position);
        }

        Set<Integer> posSet = rowMap.keySet();
        //获取最后一个已经存在的Row
        int lastPos = Collections.max(posSet);
        if (position > lastPos) {
            Row lastRow = rowMap.get(lastPos);
            View view = lastRow.lastView();
            int startPos = layoutManager.getPosition(view);
            return newRowFrom(startPos + 1, lastPos + 1, lastRow.getEnd());
        }
        return null;
    }

    private Row newRowFrom(int startPos, int rowIndex, int coordinate) {
        int itemCount = state.getItemCount();
        if (startPos > itemCount - 1)
            return null;
        int width = layoutManager.getWidth(), viewWidth;
        Row row = new Row(layoutManager);
        for (int i = startPos; i < itemCount; i++) {
            View view = recycler.getViewForPosition(i);
            layoutManager.measureChild(view, 0, 0);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            viewWidth = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            if (viewWidth <= width) {
                width -= viewWidth;
                row.addView(view);
            } else break;
        }
        row.setCoordinate(coordinate);
        row.setIndex(rowIndex);
        rowMap.put(rowIndex, row);
        return row;

    }

    public RecyclerView.Recycler real() {
        return recycler;
    }

    public void prepareRowsFromStart() {
        int width = layoutManager.getWidth();
        int remainingHeight = layoutManager.getHeight();
        int remainingWidth = width;
        int viewWidth;
        int start = 0, rowIndex = 0;
        Row row = new Row(layoutManager);
        int itemCount = state.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            if (remainingHeight <= 0)
                break;
            View view = recycler.getViewForPosition(i);
            layoutManager.measureChild(view, 0, 0);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            viewWidth = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            if (viewWidth <= remainingWidth) {
                remainingWidth -= viewWidth;
                row.addView(view);
            } else if (viewWidth > remainingWidth) {
                remainingHeight -= row.getRowHeight();
                row.setCoordinate(start);
                start += row.getRowHeight();
                rowMap.put(rowIndex++, row);
                row = new Row(layoutManager);
                row.setIndex(rowIndex-1);
                row.addView(view);
                remainingWidth = width - viewWidth;
            }

            if (i == itemCount - 1) {
                row.setCoordinate(start);
                row.setIndex(rowIndex);
                rowMap.put(rowIndex, row);
            }
        }
    }

    public Row getRowForView(View view) {
        Set<Map.Entry<Integer, Row>> entries = rowMap.entrySet();
        for (Map.Entry<Integer, Row> entry :
                entries) {
            if (entry.getValue().has(view))
                return entry.getValue();

        }
        return null;
    }

    public void updateRowCoordinate(int scrolled) {
        Set<Map.Entry<Integer, Row>> entries = rowMap.entrySet();
        for (Map.Entry<Integer, Row> entry :
                entries) {
            entry.getValue().updateCoordinate(scrolled);

        }
    }
}
