package flow.hiray.com;

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

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0 && state.isPreLayout())
            return;
        updateAnchorInfo(recycler, state);
        detachAndScrapAttachedViews(recycler);
        int extra;
        if (state.isPreLayout()) {

        }
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
