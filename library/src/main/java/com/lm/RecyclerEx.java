package com.lm;

import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.View;

/**
 * Created by hiray on 2018/1/29.
 *
 * @author hiray
 */

public class RecyclerEx {
    private RecyclerView.Recycler recycler;
    ArrayMap<Integer, Row> rowMap = new ArrayMap<>();

    public RecyclerEx(RecyclerView.Recycler recycler) {
        this.recycler = recycler;
    }

    //获取对应行的Row
    //如果没有则找出已有row中最大的View 往后继续创建一行
    public Row getRowForPosition(int position) {
        if (rowMap.containsKey(position)) {
            return rowMap.get(position);
        }

        View child = recycler.getViewForPosition(position);
        Row row = newRowFrom(position);
        rowMap.put(position, row);
        return row;
    }

    public RecyclerView.Recycler real() {
        return recycler;
    }
}
