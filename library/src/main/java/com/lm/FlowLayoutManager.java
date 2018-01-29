package com.lm;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Created by hiray on 2018/1/27.
 *
 * @author hiray
 */

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    public static final int INVALID_OFFSET = Integer.MIN_VALUE;
    List<Row> rows = new ArrayList<>();
    private View rowClosestToStart;
    private RecyclerEx mRecyclerEx;
    private AnchorRow mAnchorInfo = new AnchorRow();

    public FlowLayoutManager() {
        setAutoMeasureEnabled(true);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        ensureRecyclerEx(recycler);
        onLayoutChildrenInternal(mRecyclerEx, state);
    }

    private void ensureRecyclerEx(RecyclerView.Recycler recycler) {
        if (mRecyclerEx == null)
            mRecyclerEx = new RecyclerEx(recycler);
    }

    private void onLayoutChildrenInternal(RecyclerEx recyclerEx, RecyclerView.State state) {
        updateAnchorInfo(mRecyclerEx, state, mAnchorInfo);
        detachAndScrapAttachedViews(recyclerEx.real());
        updateLayoutState();
    }

    private int fill(RecyclerEx recyclerEx, LayoutState layoutState, RecyclerView.State state) {
        int width = getWidth();
        int remainingHeight = getHeight();
        int remainingWidth = width;
        int viewWidth;
        int start = 0;
        Row row = new Row(this);
        for (int i = 0; i < state.getItemCount(); i++) {
            if (remainingHeight <= 0)
                break;
            View view = recyclerEx.getViewForPosition(i);
            measureChild(view, 0, 0);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
            viewWidth = view.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            if (viewWidth <= remainingWidth) {
                remainingWidth -= viewWidth;
                row.addView(view);
            } else if (viewWidth > remainingWidth) {
                remainingHeight -= row.getRowHeight();
                row.setCoordinate(start);
                start += row.getRowHeight();
                row.layout();
                rows.add(row);
                row = new Row(this);
                row.addView(view);
                remainingWidth = width - viewWidth;
            }
        }
        return 0;
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dy, recycler, state);
    }

    private LayoutState mLayoutState = new LayoutState();

    private int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollInternal(dy, RecyclerEx.wrap(recycler), state);
    }

    private int scrollInternal(int dy, RecyclerEx wrap, RecyclerView.State state) {
        return 0;
    }

    private void updateLayoutState(int layoutDirection, int absDy, RecyclerView.State state) {

    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    private void updateAnchorInfo(RecyclerEx recyclerEx, RecyclerView.State state, AnchorRow anchorInfo) {
        //rows is empty,wo need build from scratch
//        if (updateAnchorFromPendingData(state, anchorInfo)) {
//            if (DEBUG) {
//                Log.d(TAG, "updated anchor info from pending information");
//            }
//            return;
//        }

        if (updateAnchorFromRow(mRecyclerEx, state, anchorInfo)) {
            return;
        }
        anchorInfo.assignCoordinateFromPadding();
        anchorInfo.mPosition = /*mStackFromEnd ? state.getItemCount() - 1 :*/ 0;

    }

    private boolean updateAnchorFromRow(RecyclerEx mRecyclerEx, RecyclerView.State state, AnchorRow anchorInfo) {
        return false;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }


    public View getRowClosestToStart() {
        return rowClosestToStart;
    }

    class LayoutState {

        static final String TAG = "LLM#LayoutState";

        static final int LAYOUT_START = -1;

        static final int LAYOUT_END = 1;

        static final int INVALID_LAYOUT = Integer.MIN_VALUE;

        static final int ITEM_DIRECTION_HEAD = -1;

        static final int ITEM_DIRECTION_TAIL = 1;

        static final int SCROLLING_OFFSET_NaN = Integer.MIN_VALUE;

        /**
         * We may not want to recycle children in some cases (e.g. layout)
         */
        boolean mRecycle = true;

        /**
         * Pixel offset where layout should start
         */
        int mOffset;

        /**
         * Number of pixels that we should fill, in the layout direction.
         */
        int mAvailable;

        /**
         * Current position on the adapter to get the next item.
         */
        int mCurrentPosition;

        /**
         * Defines the direction in which the data adapter is traversed.
         * Should be {@link #ITEM_DIRECTION_HEAD} or {@link #ITEM_DIRECTION_TAIL}
         */
        int mItemDirection;

        /**
         * Defines the direction in which the layout is filled.
         * Should be {@link #LAYOUT_START} or {@link #LAYOUT_END}
         */
        int mLayoutDirection;

        /**
         * Used when LayoutState is constructed in a scrolling state.
         * It should be set the amount of scrolling we can make without creating a new view.
         * Settings this is required for efficient view recycling.
         */
        int mScrollingOffset;

        /**
         * Used if you want to pre-layout items that are not yet visible.
         * The difference with {@link #mAvailable} is that, when recycling, distance laid out for
         * {@link #mExtra} is not considered to avoid recycling visible children.
         */
        int mExtra = 0;

        /**
         * Equal to {@link RecyclerView.State#isPreLayout()}. When consuming scrap, if this value
         * is set to true, we skip removed views since they should not be laid out in post layout
         * step.
         */
        boolean mIsPreLayout = false;

        /**
         * The most recent {@link #scrollBy(int, RecyclerView.Recycler, RecyclerView.State)}
         * amount.
         */
        int mLastScrollDelta;


    }

    class AnchorRow {

        int mPosition;
        int mCoordinate;
        boolean mLayoutFromEnd;
        boolean mValid;

        AnchorRow() {
            reset();
        }

        void reset() {
            mPosition = NO_POSITION;
            mCoordinate = INVALID_OFFSET;
            mLayoutFromEnd = false;
            mValid = false;
        }

        /**
         * assigns anchor coordinate from the RecyclerView's padding depending on current
         * layoutFromEnd value
         */
        void assignCoordinateFromPadding() {
            mCoordinate = 0;
        }

        @Override
        public String toString() {
            return "AnchorInfo{"
                    + "mPosition=" + mPosition
                    + ", mCoordinate=" + mCoordinate
                    + ", mLayoutFromEnd=" + mLayoutFromEnd
                    + ", mValid=" + mValid
                    + '}';
        }

        boolean isViewValidAsAnchor(View child, RecyclerView.State state) {
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            return !lp.isItemRemoved() && lp.getViewLayoutPosition() >= 0
                    && lp.getViewLayoutPosition() < state.getItemCount();
        }

//        public void assignFromViewAndKeepVisibleRect(View child) {
//            final int spaceChange = mOrientationHelper.getTotalSpaceChange();
//            if (spaceChange >= 0) {
//                assignFromView(child);
//                return;
//            }
//            mPosition = getPosition(child);
//            if (mLayoutFromEnd) {
//                final int prevLayoutEnd = mOrientationHelper.getEndAfterPadding() - spaceChange;
//                final int childEnd = mOrientationHelper.getDecoratedEnd(child);
//                final int previousEndMargin = prevLayoutEnd - childEnd;
//                mCoordinate = mOrientationHelper.getEndAfterPadding() - previousEndMargin;
//                // ensure we did not push child's top out of bounds because of this
//                if (previousEndMargin > 0) { // we have room to shift bottom if necessary
//                    final int childSize = mOrientationHelper.getDecoratedMeasurement(child);
//                    final int estimatedChildStart = mCoordinate - childSize;
//                    final int layoutStart = mOrientationHelper.getStartAfterPadding();
//                    final int previousStartMargin = mOrientationHelper.getDecoratedStart(child)
//                            - layoutStart;
//                    final int startReference = layoutStart + Math.min(previousStartMargin, 0);
//                    final int startMargin = estimatedChildStart - startReference;
//                    if (startMargin < 0) {
//                        // offset to make top visible but not too much
//                        mCoordinate += Math.min(previousEndMargin, -startMargin);
//                    }
//                }
//            } else {
//                final int childStart = mOrientationHelper.getDecoratedStart(child);
//                final int startMargin = childStart - mOrientationHelper.getStartAfterPadding();
//                mCoordinate = childStart;
//                if (startMargin > 0) { // we have room to fix end as well
//                    final int estimatedEnd = childStart
//                            + mOrientationHelper.getDecoratedMeasurement(child);
//                    final int previousLayoutEnd = mOrientationHelper.getEndAfterPadding()
//                            - spaceChange;
//                    final int previousEndMargin = previousLayoutEnd
//                            - mOrientationHelper.getDecoratedEnd(child);
//                    final int endReference = mOrientationHelper.getEndAfterPadding()
//                            - Math.min(0, previousEndMargin);
//                    final int endMargin = endReference - estimatedEnd;
//                    if (endMargin < 0) {
//                        mCoordinate -= Math.min(startMargin, -endMargin);
//                    }
//                }
//            }
//        }

        public void assignFromView(View child) {
//            if (mLayoutFromEnd) {
//                mCoordinate = mOrientationHelper.getDecoratedEnd(child)
//                        + mOrientationHelper.getTotalSpaceChange();
//            } else {
//                mCoordinate = mOrientationHelper.getDecoratedStart(child);
//            }
//
//            mPosition = getPosition(child);
        }
    }
}
