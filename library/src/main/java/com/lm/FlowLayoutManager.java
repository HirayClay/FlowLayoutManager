package com.lm;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Created by hiray on 2018/1/27.
 *
 * @author hiray
 */

public class FlowLayoutManager extends RecyclerView.LayoutManager {

    private static final int INVALID_OFFSET = Integer.MIN_VALUE;
    private RecyclerEx mRecyclerEx;
    private AnchorRow mAnchorInfo = new AnchorRow();
    private OrientationHelper mOrientationHelper;
    private LayoutRowResult mLayoutRowResult = new LayoutRowResult();

    public FlowLayoutManager() {
        setAutoMeasureEnabled(true);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        ensureLayoutState();
        ensureRecyclerEx(recycler, state);
        onLayoutChildrenInternal(mRecyclerEx, state);
    }

    private void ensureLayoutState() {
        if (mLayoutState == null)
            mLayoutState = new LayoutState();
        if (mOrientationHelper == null)
            mOrientationHelper = OrientationHelper.create(this);
    }

    private void ensureRecyclerEx(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mRecyclerEx == null)
            mRecyclerEx = new RecyclerEx(recycler, this, state);
    }

    private void onLayoutChildrenInternal(RecyclerEx recyclerEx, RecyclerView.State state) {
        updateAnchorInfo(mRecyclerEx, state, mAnchorInfo);
        prepareRows(recyclerEx);
        detachAndScrapAttachedViews(recyclerEx.real());
        updateLayoutStateToFillEnd(mAnchorInfo);
        fill(recyclerEx, mLayoutState, state);
    }

    private void updateLayoutStateToFillEnd(AnchorRow anchorInfo) {
        updateLayoutStateToFillEnd(anchorInfo.mPosition, anchorInfo.mCoordinate);
    }

    private void updateLayoutStateToFillEnd(int itemPosition, int offset) {
        mLayoutState.mAvailable = mOrientationHelper.getEndAfterPadding() - offset;
        mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
        mLayoutState.mCurrentPosition = itemPosition;
        mLayoutState.mLayoutDirection = LayoutState.LAYOUT_END;
        mLayoutState.mOffset = offset;
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
    }

    private void prepareRows(RecyclerEx recyclerEx) {
        if (mAnchorInfo.mCoordinate == 0) {
            recyclerEx.prepareRowsFromStart();
        }
    }

    private int fill(RecyclerEx recyclerEx, LayoutState layoutState, RecyclerView.State state) {

        final int start = layoutState.mAvailable;

        int remainingSpace = start + layoutState.mExtra;
        LayoutRowResult layoutRowResult = mLayoutRowResult;
        while (remainingSpace > 0 && layoutState.hasMore(state)) {
            layoutRowResult.resetInternal();
            layoutChunk(recyclerEx, layoutState, layoutRowResult);
            layoutState.mOffset += layoutRowResult.mConsumed * layoutState.mLayoutDirection;
            if (!layoutRowResult.mIgnoreConsumed || !state.isPreLayout()/* || layoutState.mScrapList != null*/)
                remainingSpace -= layoutRowResult.mConsumed;
            if (layoutRowResult.mFinished)
                break;
            if (layoutState.mScrollingOffset != LayoutState.SCROLLING_OFFSET_NaN) {
                layoutState.mScrollingOffset += layoutRowResult.mConsumed;
                if (layoutState.mAvailable < 0) {
                    layoutState.mScrollingOffset += layoutState.mAvailable;
                }
//                recycleByLayoutState(recycler, layoutState);
            }
        }
        return start - layoutState.mAvailable;
    }

    private void layoutChunk(RecyclerEx recyclerEx,
                             LayoutState layoutState, LayoutRowResult layoutRowResult) {
        Row row = layoutState.next(recyclerEx);
        if (row == null) {
            layoutRowResult.mFinished = true;
            return;
        }
        row.layout();
        layoutRowResult.mIgnoreConsumed = false;
        layoutRowResult.mConsumed = row.getRowHeight();
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollBy(dy, recycler, state);
    }

    private LayoutState mLayoutState = new LayoutState();

    @SuppressWarnings("unused")
    private int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return scrollInternal(dy, mRecyclerEx, state);
    }

    /*处理滑动*/
    private int scrollInternal(int dy, RecyclerEx recyclerEx, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        mLayoutState.mRecycle = true;
        ensureLayoutState();
        final int layoutDirection = dy > 0 ? LayoutState.LAYOUT_END : LayoutState.LAYOUT_START;
        final int absDy = Math.abs(dy);
        updateLayoutState(layoutDirection, absDy, state);
        final int consumed = mLayoutState.mScrollingOffset
                + fill(recyclerEx, mLayoutState, state);
        if (consumed < 0) {
            return 0;
        }
        final int scrolled = absDy > consumed ? layoutDirection * consumed : dy;
        mOrientationHelper.offsetChildren(-scrolled);
        mLayoutState.mLastScrollDelta = scrolled;
        mRecyclerEx.updateRowCoordinate(-scrolled);
        return scrolled;
    }

    @SuppressWarnings("unused")
    private void updateLayoutState(int layoutDirection, int requiredSpace, RecyclerView.State state) {
        mLayoutState.mLayoutDirection = layoutDirection;
        int scrollingOffset;
        if (layoutDirection == LayoutState.LAYOUT_END) {
            Row row = getRowClosestToEnd();
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_TAIL;
            mLayoutState.mCurrentPosition = row.index + mLayoutState.mItemDirection;
            mLayoutState.mOffset = row.getEnd();
            // calculate how much we can scroll without adding new children (independent of layout)
            scrollingOffset = row.getEnd()
                    - mOrientationHelper.getEndAfterPadding();
        } else {
            Row row = getRowClosestToStart();
            mLayoutState.mItemDirection = LayoutState.ITEM_DIRECTION_HEAD;
            mLayoutState.mCurrentPosition = row.index + mLayoutState.mItemDirection;
            mLayoutState.mOffset = row.getStart();
            // calculate how much we can scroll without adding new children (independent of layout)
            scrollingOffset = -row.getStart()
                    + mOrientationHelper.getStartAfterPadding();
        }

        mLayoutState.mAvailable = requiredSpace;
        mLayoutState.mScrollingOffset = scrollingOffset;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    private boolean updateAnchorFromRow(RecyclerEx mRecyclerEx, RecyclerView.State state, AnchorRow anchorInfo) {
        return false;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.WRAP_CONTENT);
    }


    private Row getRowClosestToStart() {
        return mRecyclerEx.getRowForView(getChildAt(0));
    }

    private Row getRowClosestToEnd() {
        return mRecyclerEx.getRowForView(getChildAt(getChildCount() - 1));
    }

    class LayoutState {

        static final int LAYOUT_START = -1;

        static final int LAYOUT_END = 1;

//        static final int INVALID_LAYOUT = Integer.MIN_VALUE;

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
         * mExtra is not considered to avoid recycling visible children.
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

        boolean hasMore(RecyclerView.State state) {
            return mCurrentPosition >= 0 && mCurrentPosition < state.getItemCount();
        }

        Row next(RecyclerEx recyclerEx) {
//            if (mScrapList != null) {
//                return nextViewFromScrapList();
//            }
            final Row row = recyclerEx.getRowForPosition(mCurrentPosition);
            mCurrentPosition += mItemDirection;
            return row;
        }


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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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

    class LayoutRowResult {

        int mConsumed;
        boolean mFinished;
        boolean mIgnoreConsumed;
        boolean mFocusable;

        void resetInternal() {
            mConsumed = 0;
            mFinished = false;
            mIgnoreConsumed = false;
            mFocusable = false;
        }
    }
}
