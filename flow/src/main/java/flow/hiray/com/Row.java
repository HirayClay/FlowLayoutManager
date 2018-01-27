package flow.hiray.com;

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

    public int getHeight() {
        if (NaN != rowHeight)
            return rowHeight;
        int h = 0;
        for (View v :
                views) {
            int vh = v.getMeasuredHeight();
            if (vh > h)
                h = vh;
        }
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
}
