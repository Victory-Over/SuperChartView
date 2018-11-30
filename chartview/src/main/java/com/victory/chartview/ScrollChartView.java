package com.victory.chartview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * describe：
 *
 * @author ：鲁宇峰 on 2018/11/14 14：34
 *         email：466708987@qq.com
 *         github：https://github.com/Victory-Over
 */
public class ScrollChartView extends View {

    private int mBeginRange = 0;

    private int mEndRange;

    private int mInnerWidth;

    /**
     * 画笔 指示标
     */
    private Paint mIndicatePaint;

    /**
     * 指示标 颜色
     */
    private int mIndicateColor;

    /**
     * 指示标 宽度
     */
    private int mIndicateWidth;


    /**
     * 指示标 高度
     */
    private int mIndicateHeight;

    /**
     * 指示标 左右间隔
     */
    private int mIndicatePadding;

    /**
     * 指示器 间隔底部的距离
     */
    private int mIndicateBottomPadding;

    /**
     * 画笔 文字
     */
    private Paint mTextPaint;

    /**
     * 文字 默认颜色
     */
    private int mTextColor;

    /**
     * 文字 默认大小
     */
    private float mTextSize;

    /**
     * 文字 选中大小
     */
    private float mTextSelectedSize;

    /**
     * 文字 间隔底部的距离
     */
    private int mTextBottomPadding;

    /**
     * 画笔 线
     */
    private Paint mLinePaint;

    /**
     * 线 起始颜色
     */
    private int mLineStartColor;

    /**
     * 线 结束颜色
     */
    private int mLineEndColor;


    /**
     * 线 宽度
     */
    private int mLineWidth;

    /**
     * 线 类型
     */
    private LineType mLineType = LineType.ARC;

    /**
     * 阴影 画笔
     */
    private Paint mShadowPaint;

    /**
     * 阴影 渐变开始颜色
     */
    private int mShadowStartColor;

    /**
     * 阴影 渐变结束颜色
     */
    private int mShadowEndColor;

    /**
     * 网格线 画笔
     */
    private Paint mGridPaint;

    /**
     * 网格线 颜色
     */
    private int mGirdColor;

    /**
     * 网格线 宽度
     */
    private int mGridWith;


    /**
     * 底部文字和指示标的高度
     */
    private int mShadowMarginHeight;

    /**
     * 选中颜色
     */
    private int mSelectedColor;

    private int mLastMotionX;

    private boolean mIsDragged;

    private boolean mIsAutoAlign = true;


    /**
     * 滚动后选择监听
     */
    private OnScaleListener mListener;

    private int mGravity = Gravity.TOP;

    private Rect mIndicateLoc;

    /**
     * 滚动相关参数
     */
    private OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    /**
     * X轴坐标值
     */
    private List<String> timeList;
    /**
     * Y轴坐标值
     */
    private List<Double> dataList;
    /**
     * 坐标集合
     */
    private List<Point> mList = new ArrayList<>();
    /**
     * Y轴坐标最大的数据
     */
    private double maxData;

    /**
     * 当前选中的下标
     */
    private int position;

    public ScrollChartView(Context context) {
        this(context, null);
    }

    public ScrollChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化一些参数
        mSelectedColor = ContextCompat.getColor(context, R.color.colorSelected);

        mLineStartColor = ContextCompat.getColor(context, R.color.colorLineStart);
        mLineEndColor = ContextCompat.getColor(context, R.color.colorLineEnd);
        mLineWidth = UIUtils.dp2px(context, 2);

        mTextColor = ContextCompat.getColor(context, R.color.colorText);
        mTextSize = UIUtils.dp2px(context, 9);
        mTextSelectedSize = UIUtils.dp2px(context, 16);
        mTextBottomPadding = UIUtils.dp2px(getContext(), 1);

        mIndicateHeight = UIUtils.dp2px(context, 5);
        mIndicateWidth = UIUtils.dp2px(context, 2);
        mIndicateColor = Color.WHITE;
        mIndicatePadding = UIUtils.dp2px(getContext(), 25);
        mIndicateBottomPadding = UIUtils.dp2px(getContext(), 15);

        mShadowStartColor = ContextCompat.getColor(getContext(), R.color.colorShadowStart);
        mShadowEndColor = ContextCompat.getColor(getContext(), R.color.colorShadowEnd);
        mShadowMarginHeight = UIUtils.dp2px(getContext(), 30);

        mGirdColor = ContextCompat.getColor(context, R.color.colorGrid);
        mGridWith = UIUtils.dp2px(context, 1);

        initValue();
    }

    public void setData(List<String> times, List<Double> dataList) {
        Double min = Collections.min(dataList);
        //如果数据里面有负数 则需要将每个数据都减去最大的负数 以防止负数出现
        if (min < 0) {
            this.dataList.clear();
            for (int i = 0; i < dataList.size(); i++) {
                this.dataList.add(dataList.get(i) - min);
            }
        } else {
            this.dataList = dataList;
        }
        this.timeList = times;
        maxData = Collections.max(this.dataList);

        mEndRange = times.size() - 1;
        initValue();
        getPointList();
        invalidate();
    }

    private void initValue() {

        mOverScroller = new OverScroller(getContext());
        setOverScrollMode(OVER_SCROLL_ALWAYS);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();


        mIndicatePaint = new Paint();
        mIndicatePaint.setStyle(Paint.Style.FILL);
        mIndicatePaint.setAntiAlias(true);

        mLinePaint = new Paint();
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mLineWidth);
        Shader lineShader = new LinearGradient(0, 0, getWidth(), getHeight(), mLineEndColor, mLineStartColor, Shader.TileMode.CLAMP);
        mLinePaint.setShader(lineShader);


        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setAntiAlias(true);
        mInnerWidth = (mEndRange - mBeginRange) * getIndicateWidth();

        mShadowPaint = new Paint();
        mShadowPaint.setStyle(Paint.Style.FILL);
        mShadowPaint.setAntiAlias(true);
        Shader shader = new LinearGradient(getWidth() / 2, getHeight(), getWidth() / 2, 0, mShadowEndColor, mShadowStartColor, Shader.TileMode.MIRROR);
        mShadowPaint.setShader(shader);

        mGridPaint = new Paint();
        mGridPaint.setStyle(Paint.Style.FILL);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(mGirdColor);
        mGridPaint.setStrokeWidth(mGridWith);

        mIndicateLoc = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (timeList == null) {
            return;
        }
        int count = canvas.save();

        //画网格
        drawGridLine(canvas);

        //画线
        if (LineType.ARC == mLineType) {
            //曲线
            drawScrollLine(canvas);
        } else {
            //折线
            drawLine(canvas);
        }
        //阴影
        drawShadow(canvas);
        for (int value = mBeginRange, position = 0; value <= mEndRange; value++, position++) {
            drawIndicate(canvas, position);
            drawText(canvas, position, timeList.get(value));
        }

        canvas.restoreToCount(count);
    }

    /**
     * 绘制网格线
     *
     * @param canvas
     */
    private void drawGridLine(Canvas canvas) {
        for (int i = 0; i < mList.size(); i++) {
            computeIndicateLoc(mIndicateLoc, i);
            int left = mIndicateLoc.left + mIndicatePadding;
            int right = mIndicateLoc.right - mIndicatePadding;
            int bottom = getHeight() - mShadowMarginHeight;
            mGridPaint.setColor(mGirdColor);
            canvas.drawRect(left, 0, right, bottom, mGridPaint);
        }
    }

    /**
     * 绘制指示标
     */
    private void drawIndicate(Canvas canvas, int position) {
        computeIndicateLoc(mIndicateLoc, position);
        int left = mIndicateLoc.left + mIndicatePadding;
        int right = mIndicateLoc.right - mIndicatePadding;
        int bottom = mIndicateLoc.bottom;
        int top = bottom - mIndicateHeight;
        if (this.position == position) {
            mIndicatePaint.setColor(mSelectedColor);
        } else {
            mIndicatePaint.setColor(mIndicateColor);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(left, top, right, bottom, 5, 5, mIndicatePaint);
        } else {
            canvas.drawRect(left, top, right, bottom, mIndicatePaint);
        }
    }

    /**
     * 绘制文字
     */
    private void drawText(Canvas canvas, int position, String text) {
        computeIndicateLoc(mIndicateLoc, position);

        if (this.position == position) {
            mTextPaint.setTextSize(mTextSelectedSize);
            mTextPaint.setColor(mSelectedColor);
        } else {
            mTextPaint.setTextSize(mTextSize);
            mTextPaint.setColor(mTextColor);
        }

        int x = (mIndicateLoc.left + mIndicateLoc.right) / 2;
        int y = mIndicateLoc.bottom + mIndicateBottomPadding - mTextBottomPadding;

        if (!isAlignTop()) {
            y = mIndicateLoc.top;
            mTextPaint.getTextBounds(text, 0, text.length(), mIndicateLoc);
            //增加一些偏移
            y += mIndicateLoc.top / 2;
        }

        canvas.drawText(text, x, y, mTextPaint);
    }

    /**
     * 绘制折线图
     */
    private void drawLine(Canvas canvas) {
        Path path = new Path();
        path.moveTo(mList.get(0).x, mList.get(0).y);
        for (int i = 1; i < mList.size(); i++) {
            path.lineTo(mList.get(i).x, mList.get(i).y);
        }
        canvas.drawPath(path, mLinePaint);
    }

    /**
     * 绘制曲线图
     */
    private void drawScrollLine(Canvas canvas) {
        Point pStart;
        Point pEnd;
        Path path = new Path();
        for (int i = 0; i < mList.size() - 1; i++) {
            pStart = mList.get(i);
            pEnd = mList.get(i + 1);
            Point point3 = new Point();
            Point point4 = new Point();
            float wd = (pStart.x + pEnd.x) / 2;
            point3.x = wd;
            point3.y = pStart.y;
            point4.x = wd;
            point4.y = pEnd.y;
            path.moveTo(pStart.x, pStart.y);
            path.cubicTo(point3.x, point3.y, point4.x, point4.y, pEnd.x, pEnd.y);
            canvas.drawPath(path, mLinePaint);
        }
    }

    /**
     * 绘制阴影
     */
    private void drawShadow(Canvas canvas) {
        if (mLineType == LineType.ARC) {
            Point pStart;
            Point pEnd;
            Path path = new Path();
            for (int i = 0; i < mList.size() - 1; i++) {
                pStart = mList.get(i);
                pEnd = mList.get(i + 1);
                Point point3 = new Point();
                Point point4 = new Point();
                float wd = (pStart.x + pEnd.x) / 2;
                point3.x = wd;
                point3.y = pStart.y;
                point4.x = wd;
                point4.y = pEnd.y;
                path.moveTo(pStart.x, pStart.y);
                path.cubicTo(point3.x, point3.y, point4.x, point4.y, pEnd.x, pEnd.y);
                //减去文字和指示标的高度
                path.lineTo(pEnd.x, getHeight() - mShadowMarginHeight);
                path.lineTo(pStart.x, getHeight() - mShadowMarginHeight);
            }
            path.close();
            canvas.drawPath(path, mShadowPaint);
        } else {
            Path path = new Path();
            path.moveTo(mList.get(0).x, mList.get(0).y);
            for (int i = 1; i < mList.size(); i++) {
                path.lineTo(mList.get(i).x, mList.get(i).y);
            }
            //链接最后两个点
            int index = mList.size() - 1;
            path.lineTo(mList.get(index).x, getHeight() - mShadowMarginHeight);
            path.lineTo(mList.get(0).x, getHeight() - mShadowMarginHeight);
            path.close();
            canvas.drawPath(path, mShadowPaint);
        }
    }

    /**
     * 获取每个数据源的坐标
     */
    private void getPointList() {
        mList.clear();
        for (int i = 0; i < dataList.size(); i++) {
            computeIndicateLoc(mIndicateLoc, i);
            int left = mIndicateLoc.left + mIndicatePadding + (mIndicateWidth / 2);
            // 获取view的高度 减去所以控件的高度 得到 图表的高度 8代表预留出来的边距
            int height = getHeight() - mShadowMarginHeight - UIUtils.dp2px(getContext(), 8) - mLineWidth;
            // 通过每个数据除以最大的数据 得到所占比
            double scale = dataList.get(i) / maxData;
            // 图表高度减数据高度 得到每个数据的坐标点
            int top = height - (int) (height * scale) + UIUtils.dp2px(getContext(), 8);
            Point point = new Point();
            point.x = left;
            point.y = top;
            mList.add(point);
        }
    }

    /**
     * 计算indicate的位置
     *
     * @param outRect
     * @param position
     */
    private void computeIndicateLoc(Rect outRect, int position) {
        if (outRect == null) {
            return;
        }

        int height = getHeight();
        int indicate = getIndicateWidth();

        int left = (indicate * position);
        int right = left + indicate;
        int top = getPaddingTop();
        int bottom = height - getPaddingBottom();

        if (isAlignTop()) {
            bottom -= mIndicateBottomPadding;
        } else {
            top += mIndicateBottomPadding;
        }

        outRect.set(left, top, right, bottom);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();

        mVelocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //
                if (mIsDragged = !mOverScroller.isFinished()) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }

                mLastMotionX = (int) event.getX();

                return true;

            case MotionEvent.ACTION_MOVE:

                int curX = (int) event.getX();
                int deltaX = mLastMotionX - curX;

                if (!mIsDragged && Math.abs(deltaX) > mTouchSlop) {
                    if (getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }

                    mIsDragged = true;

                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }

                if (mIsDragged) {
                    mLastMotionX = curX;

                    if (getScrollX() <= 0 || getScrollX() >= getMaximumScroll()) {
                        deltaX *= 0.7;
                    }


                    if (overScrollBy(deltaX, 0, getScrollX(), getScrollY(), getMaximumScroll(), 0, getWidth(), 0, true)) {
                        mVelocityTracker.clear();
                    }

                }

                break;
            case MotionEvent.ACTION_UP: {
                if (mIsDragged) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) mVelocityTracker.getXVelocity();

                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        fling(-initialVelocity);
                    } else {
                        //alignCenter();
                        sprintBack();
                    }
                }

                mIsDragged = false;
                recycleVelocityTracker();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {

                if (mIsDragged && mOverScroller.isFinished()) {
                    sprintBack();
                }

                mIsDragged = false;

                recycleVelocityTracker();
                break;
            }
            default:
                break;
        }

        return true;
    }


    private void refreshValues() {
        mInnerWidth = (mEndRange - mBeginRange) * getIndicateWidth();
        invalidateView();

    }

    private int getIndicateWidth() {
        return mIndicateWidth + mIndicatePadding + mIndicatePadding;
    }

    /**
     * 获取最小滚动值。
     *
     * @return
     */
    private int getMinimumScroll() {
        return -(getWidth() - getIndicateWidth()) / 2;
    }

    /**
     * 获取最大滚动值。
     *
     * @return
     */
    private int getMaximumScroll() {
        return mInnerWidth + getMinimumScroll();
    }

    /**
     * 调整indicate，使其居中。
     */
    private void adjustIndicate() {
        if (!mOverScroller.isFinished()) {
            mOverScroller.abortAnimation();
        }

        int position = computeSelectedPosition();
        int scrollX = getScrollByPosition(position);
        scrollX -= getScrollX();
        this.position = position;

        if (scrollX != 0) {
            mOverScroller.startScroll(getScrollX(), getScrollY(), scrollX, 0);
            invalidateView();
        }

        //滚动完毕回调
        onScaleChanged(position);
    }

    public void fling(int velocityX) {
        mOverScroller.fling(getScrollX(), getScrollY(), velocityX, 0, getMinimumScroll(), getMaximumScroll(), 0, 0, getWidth() / 2, 0);
        invalidateView();
    }

    public void sprintBack() {
        mOverScroller.springBack(getScrollX(), getScrollY(), getMinimumScroll(), getMaximumScroll(), 0, 0);
        invalidateView();
    }


    public void setOnScaleListener(OnScaleListener listener) {
        if (listener != null) {
            mListener = listener;
        }
    }

    /**
     * 获取position的绝对滚动位置。
     *
     * @param position
     * @return
     */
    private int getScrollByPosition(int position) {
        computeIndicateLoc(mIndicateLoc, position);
        int scrollX = mIndicateLoc.left + getMinimumScroll();
        return scrollX;
    }

    /**
     * 计算当前已选择的位置
     *
     * @return
     */
    public int computeSelectedPosition() {
        int centerX = getScrollX() - getMinimumScroll() + getIndicateWidth() / 2;
        centerX = Math.max(0, Math.min(mInnerWidth, centerX));
        int position = centerX / getIndicateWidth();
        return position;
    }

    public void smoothScrollTo(int position) {
        if (position < 0 || mBeginRange + position > mEndRange) {
            return;
        }

        if (!mOverScroller.isFinished()) {
            mOverScroller.abortAnimation();
        }

        int scrollX = getScrollByPosition(position);
        mOverScroller.startScroll(getScrollX(), getScrollY(), scrollX - getScrollX(), 0);
        invalidateView();
    }

    public void smoothScrollToValue(int value) {
        int position = value - mBeginRange;
        smoothScrollTo(position);
    }


    /**
     * 滚动回调
     */
    private void onScaleChanged(int position) {
        if (mListener != null) {
            mListener.onScaleChanged(position);
        }
    }


    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (!mOverScroller.isFinished()) {
            final int oldX = getScrollX();
            final int oldY = getScrollY();
            setScrollX(scrollX);
            onScrollChanged(scrollX, scrollY, oldX, oldY);
            if (clampedX) {
                //sprintBack();
            }
        } else {
            super.scrollTo(scrollX, scrollY);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


    }

    private boolean isAlignTop() {
        return (mGravity & Gravity.TOP) == Gravity.TOP;
    }


    public void setGravity(int gravity) {
        this.mGravity = gravity;
        invalidateView();
    }

    @Override
    public void computeScroll() {
        if (this.mOverScroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = this.mOverScroller.getCurrX();
            int y = this.mOverScroller.getCurrY();
            overScrollBy(x - oldX, y - oldY, oldX, oldY, getMaximumScroll(), 0, getWidth(), 0, false);
            invalidateView();
        } else if (!mIsDragged && mIsAutoAlign) {
            adjustIndicate();
        }

    }

    @Override
    protected int computeHorizontalScrollRange() {
        return getMaximumScroll();
    }


    public void invalidateView() {
        if (Build.VERSION.SDK_INT >= 16) {
            postInvalidateOnAnimation();
        } else {
            invalidate();
        }
    }


    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public interface OnScaleListener {
        void onScaleChanged(int position);

    }

    public void setIndicateWidth(int indicateWidth) {
        this.mIndicateWidth = indicateWidth;
        refreshValues();
    }

    public void setIndicatePadding(int indicatePadding) {
        this.mIndicatePadding = indicatePadding;
        refreshValues();
    }

    public int getIndicateHeight() {
        return mIndicateHeight;
    }

    public void setIndicateHeight(int mIndicateHeight) {
        this.mIndicateHeight = mIndicateHeight;
    }


    public void setAutoAlign(boolean autoAlign) {
        this.mIsAutoAlign = autoAlign;
        refreshValues();
    }


    public boolean isAutoAlign() {
        return mIsAutoAlign;
    }


    /**
     * 枚举类型直线或者是弧线
     */
    public enum LineType {
        LINE, ARC
    }

    public void setLineType(LineType mLineType) {
        this.mLineType = mLineType;
    }

    public LineType getLineType() {
        return mLineType;
    }

    public class Point {

        public float x;
        public float y;

        public Point() {

        }

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public List<Point> getList() {
        return mList;
    }


    public int getmIndicateColor() {
        return mIndicateColor;
    }

    public void setmIndicateColor(int mIndicateColor) {
        this.mIndicateColor = mIndicateColor;
    }

    public int getmIndicateWidth() {
        return mIndicateWidth;
    }

    public void setmIndicateWidth(int mIndicateWidth) {
        this.mIndicateWidth = mIndicateWidth;
    }

    public int getmIndicateHeight() {
        return mIndicateHeight;
    }

    public void setmIndicateHeight(int mIndicateHeight) {
        this.mIndicateHeight = mIndicateHeight;
    }

    public int getmIndicatePadding() {
        return mIndicatePadding;
    }

    public void setmIndicatePadding(int mIndicatePadding) {
        this.mIndicatePadding = mIndicatePadding;
    }

    public int getmIndicateBottomPadding() {
        return mIndicateBottomPadding;
    }

    public void setmIndicateBottomPadding(int mIndicateBottomPadding) {
        this.mIndicateBottomPadding = mIndicateBottomPadding;
    }

    public int getmTextColor() {
        return mTextColor;
    }

    public void setmTextColor(int mTextColor) {
        this.mTextColor = mTextColor;
    }

    public float getmTextSize() {
        return mTextSize;
    }

    public void setmTextSize(float mTextSize) {
        this.mTextSize = mTextSize;
    }

    public float getmTextSelectedSize() {
        return mTextSelectedSize;
    }

    public void setmTextSelectedSize(float mTextSelectedSize) {
        this.mTextSelectedSize = mTextSelectedSize;
    }

    public int getmTextBottomPadding() {
        return mTextBottomPadding;
    }

    public void setmTextBottomPadding(int mTextBottomPadding) {
        this.mTextBottomPadding = mTextBottomPadding;
    }

    public int getmLineStartColor() {
        return mLineStartColor;
    }

    public void setmLineStartColor(int mLineStartColor) {
        this.mLineStartColor = mLineStartColor;
    }

    public int getmLineEndColor() {
        return mLineEndColor;
    }

    public void setmLineEndColor(int mLineEndColor) {
        this.mLineEndColor = mLineEndColor;
    }

    public int getmLineWidth() {
        return mLineWidth;
    }

    public void setmLineWidth(int mLineWidth) {
        this.mLineWidth = mLineWidth;
    }

    public int getmShadowStartColor() {
        return mShadowStartColor;
    }

    public void setmShadowStartColor(int mShadowStartColor) {
        this.mShadowStartColor = mShadowStartColor;
    }

    public int getmShadowEndColor() {
        return mShadowEndColor;
    }

    public void setmShadowEndColor(int mShadowEndColor) {
        this.mShadowEndColor = mShadowEndColor;
    }

    public int getmGirdColor() {
        return mGirdColor;
    }

    public void setmGirdColor(int mGirdColor) {
        this.mGirdColor = mGirdColor;
    }

    public int getmGridWith() {
        return mGridWith;
    }

    public void setmGridWith(int mGridWith) {
        this.mGridWith = mGridWith;
    }

    public int getmShadowMarginHeight() {
        return mShadowMarginHeight;
    }

    public void setmShadowMarginHeight(int mShadowMarginHeight) {
        this.mShadowMarginHeight = mShadowMarginHeight;
    }

    public int getmSelectedColor() {
        return mSelectedColor;
    }

    public void setmSelectedColor(int mSelectedColor) {
        this.mSelectedColor = mSelectedColor;
    }

    public boolean ismIsAutoAlign() {
        return mIsAutoAlign;
    }

    public void setmIsAutoAlign(boolean mIsAutoAlign) {
        this.mIsAutoAlign = mIsAutoAlign;
    }

    public int getmGravity() {
        return mGravity;
    }

    public void setmGravity(int mGravity) {
        this.mGravity = mGravity;
    }
}
