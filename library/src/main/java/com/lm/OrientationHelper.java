package com.lm;

import android.support.v7.widget.RecyclerView;

/**
 * Created by hiray on 2018/1/29.
 *
 * @author hiray
 */

public class OrientationHelper {

    private RecyclerView.LayoutManager layoutManager;

    public OrientationHelper(RecyclerView.LayoutManager layoutManager) {

        this.layoutManager = layoutManager;
    }

    static OrientationHelper create(RecyclerView.LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager);
    }

    public int getEndAfterPadding() {
        return layoutManager.getHeight() - layoutManager.getPaddingBottom();
    }
}
