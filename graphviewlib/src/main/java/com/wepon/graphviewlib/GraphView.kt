package com.wepon.graphviewlib

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Half.toFloat
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
* desc: 一个曲线图
* @author Wepon.Yan
* created at 2018/6/12 上午11:06
*/
class GraphView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    /**
     * 设置默认的宽高，为了美观设置固定比例，根据UI来设置的比例 6 ： 11
     * 这个值只是当外界没有设置宽高的时候才会使用到的。
     */
    private val mAspectRatio = 11 * 1.0f / 6

    /**
     * 默认一个宽度和高度值,在计算大小的时候也要用到的
     * 在 init里面初始化这些值
     */
    private var mWidth = 0
    private var mHeight = 0

    /**
     * Y轴   纵向刻度的值
     */
    var mYTextArray = arrayOf("100", "80", "60", "40","20","00")
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Y轴   文字说明
     */
    var mYTextTip = "分钟"
        set(value) {
            field = value
            invalidate()
        }

    /**
     * X轴   横向刻度的值
     */
    var mXTextArray = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
        set(value) {
            // 这里先限制了，有需要再扩展吧
            if (value.size != 7) {
                return
            }
            field = value
            invalidate()
        }


    /**
     * Y轴最大分数值
     * 这里先不公开了，按100最大来换算，有需要再自己修改。
     */
    private val mMaxScore = 100

    /**
     * 动画
     */
    private var mAnimator: ValueAnimator? = null
    /**
     * 设置数据的动画时长
     */
    var mAnimatorTime = 500L
    /**
     * 设置分数值
     */
    var mScoreData = intArrayOf(22, 66, 22, 66, 100, 20, 75)
        set(value) {
            if (value.size > 7 || value.any { it > mMaxScore }) {
                return
            }
            if (mAnimator?.isRunning == true) {
                mAnimator?.cancel()
                mAnimator = null
            }
            val mOldScoreData = field.clone()
            val mOldSize = field.size
            var newScoreData = IntArray(value.size)
            mAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = mAnimatorTime
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    if (this@GraphView.isAttachedToWindow) {
                        val percent = it.animatedValue as Float
                        value.forEachIndexed { index, newScore ->
                            val oldScore = (if (mOldSize - 1 < index) 0 else mOldScoreData[index])
                            newScoreData[index] = (oldScore + (newScore - oldScore) * percent).toInt()
                        }
                        field = newScoreData
                        invalidate()
                    }
                }
            }
            mAnimator?.start()
        }

    /**
     * 记录分数的点
     */
    private var mScorePoints = arrayListOf<Point>()


    /**
     * 横向线条的颜色
     */
    var mHorizontalLineColor = Color.parseColor("#D7D7D7")
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 坐标文字的颜色
     */
    var mXYTextColor = Color.parseColor("#B3B3B3")
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Y坐标上面的提示文字的颜色
     */
    var mYTipTextColor = Color.parseColor("#7F7F7F")
        set(value) {
            field = value
            invalidate()
        }

    /**
     * XY轴文字的大小
     */
    var mXYTextSize = 36f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * XY轴文字与轴的间距，就不单独分开写了，用同一个
     */
    var mXYTextPadding = 32f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 画XY轴文字的画笔
     */
    private var mXYTextPaint: Paint = Paint().apply {
        isAntiAlias = true
        textSize = mXYTextSize
        color = mXYTextColor
        style = Paint.Style.FILL_AND_STROKE
    }

    /**
     * 画横向线条的画笔
     */
    private var mHorizontalLinePaint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = 2f
        color = mHorizontalLineColor
        style = Paint.Style.FILL_AND_STROKE
    }

    /**
     * 画分数边界的线条宽度 px
     */
    var mScoreLineStrokeWidth = 4f
        set(value) {
            field = value
            mScorePaint.strokeWidth = value
            invalidate()
        }
    /**
     * 分数线条的颜色值
     */
    var mScoreLineColor = Color.parseColor("#53A616")
        set(value) {
            field = value
            mScorePaint.color = value
            invalidate()
        }

    /**
     * 渐变色的起始色，从下往上
     */
    var mStartGradientColor = Color.parseColor("#FFFFFFFF")
        set(value) {
            field = value
            invalidate()
        }
    /**
     * 渐变色的终止色，从下往上
     */
    var mEndGradientColor = Color.parseColor("#E0AAE5A4")
        set(value) {
            field = value
            invalidate()
        }

    /**
     * 画分数线的画笔
     */
    private var mScorePaint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = mScoreLineStrokeWidth
        color = mScoreLineColor
        style = Paint.Style.STROKE
//        strokeJoin = Paint.Join.ROUND
    }

    /**
     * 画分数内容的画笔
     */
    private var mScoreContentPaint: Paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = mScoreLineStrokeWidth
        color = mScoreLineColor
        style = Paint.Style.FILL
//        strokeJoin = Paint.Join.ROUND
    }
    /**
     * 路径内容
     */
    private var mPath = Path()

    /**
     * 分数线
     */
    private var mScoreLinePath = Path()


    init {
        // 初始化默认的值
        val displayMetrics = context.resources.displayMetrics
        mWidth = displayMetrics.widthPixels
        mHeight = (mWidth / mAspectRatio).toInt()
    }

    /**
     * 计算大小
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWidth, mHeight)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(mWidth, heightSize)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, mHeight)
        }
    }


    /**
     * 画出来
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mWidth = width
        mHeight = height
        drawCoordinateAxisAndScore(canvas)
    }

    /**
     * 直接在这里面画完了。嗯
     */
    private fun drawCoordinateAxisAndScore(canvas: Canvas?) {
        // 计算坐标轴XY 原点坐标
        val xTextRect = getXYTextBoundRect(mXTextArray[0])
        val mStartY = mHeight - xTextRect.height() - mXYTextPadding
        val mEndY = 0
        var yTextMaxWidth = 0
        mYTextArray.forEach {
            val width = mXYTextPaint.measureText(it)
            if (width > yTextMaxWidth) yTextMaxWidth = width.toInt()
        }
        val width = mXYTextPaint.measureText(mYTextTip)
        if (width > yTextMaxWidth) {
            yTextMaxWidth = width.toInt()
        }
        val mStartX = yTextMaxWidth + 1.0f
        val mEndX = mWidth
        // 竖直方向分成n个区间，画出坐标系内的横向线条
        val ySize = mYTextArray.size + 1
        val yAxisDistance = Math.abs(mStartY - mEndY)
        val yOneDistance = yAxisDistance / ySize
        // 分数能到的最高点。
        val mScoreMaxY = yOneDistance * 2 + mEndY
        val mScoreYDistance = Math.abs(mStartY - mScoreMaxY)

        // 画出 x 轴的文字
        val xTextCount = mXTextArray.size
        val xAsixDistance = Math.abs(mEndX - mStartX)
        // 减去两端后用来计算中间的文字的中心点，这里的视觉上看上去总有一点分布不平均，不知道是不是使用的api不精确还是什么原因了。
        // 知道了，计算方式有问题，假设字
        val measureXText = mXYTextPaint.measureText(mXTextArray[0])
        val xOneDistance = (xAsixDistance - measureXText) / (xTextCount - 1)
        // 在这里顺便把分数点的 x 坐标也写进去。下次就不用再计算了
        mScorePoints.clear()
        for (i in 0 until xTextCount) {
            // 第一个，文字靠左
            when (i) {
                0 -> {
                    mXYTextPaint.textAlign = Paint.Align.LEFT
                    canvas?.drawText(mXTextArray[i], mStartX, mHeight.toFloat() - 4f, mXYTextPaint)
                    mScorePoints.add(Point().apply { x = mStartX.toInt() })
                }
            // 最后一个，文字靠右
                xTextCount - 1 -> {
                    mXYTextPaint.textAlign = Paint.Align.RIGHT
                    canvas?.drawText(mXTextArray[i], mEndX.toFloat(), mHeight.toFloat() - 4f, mXYTextPaint)
                    mScorePoints.add(Point().apply { x = mEndX.toInt() })
                }
                else -> {
                    mXYTextPaint.textAlign = Paint.Align.CENTER
                    canvas?.drawText(mXTextArray[i], mStartX + measureXText / 2 + xOneDistance * i, mHeight.toFloat() - 4f, mXYTextPaint)
                    mScorePoints.add(Point().apply { x = (mStartX + measureXText / 2 + xOneDistance * i).toInt() })
                }
            }
        }

        if (mScoreData.isNotEmpty()) {

            // 计算分数点对应的Y
            mPath.reset()
            mScoreLinePath.reset()
//        // 把拐点设置成圆的形式，参数为圆的半径，这样就可以画出曲线了，？？不知道为什么这里不行，丑死了。
//        val cornerPathEffect = CornerPathEffect(100f)
//        mScorePaint.pathEffect = cornerPathEffect
            // 这里还是根据分数来遍历吧，有时候数据没有那么多就不显示后面的了。
            mScoreData.forEachIndexed { index, score ->
                mScorePoints[index].y = ((mStartY - score * 1.0f * mScoreYDistance / 100).toInt())
            }
//        mScorePoints.forEachIndexed { index, scorePoint ->
//            scorePoint.y = ((mStartY - mScoreData[index] * 1.0f * mScoreYDistance / 100).toInt())
//        }

            mPath.moveTo(mStartX, mStartY)
            mScoreData.forEachIndexed { index, _ ->
                var x1: Int
                var y1: Int
                var x2: Int
                var y2: Int
                var x3: Int
                var y3: Int
                x3 = if (index == mScoreData.size - 1) {
                    mScorePoints[index].x
                } else {
                    mScorePoints[index + 1].x
                }
                x1 = (x3 + mScorePoints[index].x) / 2
                x2 = x1
                y3 = if (index == mScoreData.size - 1) {
                    mScorePoints[index].y
                } else {
                    mScorePoints[index + 1].y
                }
                y1 = mScorePoints[index].y
                y2 = y3
                // 画出分数线条
                if (index == 0) {
                    mPath.lineTo(mScorePoints[index].x.toFloat(), mScorePoints[index].y.toFloat())
                    mScoreLinePath.moveTo(mScorePoints[index].x.toFloat(), mScorePoints[index].y.toFloat())
                } else {
//                mPath.lineTo(scorePoint.x.toFloat(), scorePoint.y.toFloat())
                }
                //曲线
                mPath.cubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())
                mScoreLinePath.cubicTo(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), x3.toFloat(), y3.toFloat())
            }
            mPath.lineTo(mScorePoints[mScoreData.size - 1].x.toFloat(), mStartY)


            // 离屏缓冲绘制
            val saveLayer = canvas!!.saveLayer(null, null, Canvas.ALL_SAVE_FLAG)
            canvas?.drawPath(mPath, mScoreContentPaint)
            val rectPaint = Paint()
            val left = mStartX
            val top = mScoreMaxY
            val right = mEndX.toFloat()
            val bottom = mStartY
            // 设置渐变的颜色
            val linearGraphView = LinearGradient(left, top, left, bottom, mEndGradientColor,
                    mStartGradientColor, Shader.TileMode.CLAMP)
            rectPaint.shader = linearGraphView
            rectPaint.xfermode = PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_ATOP)
            canvas?.drawRect(left, top, right, bottom, rectPaint)
            rectPaint.xfermode = null
            canvas.restoreToCount(saveLayer)
//        mScoreContentPaint.pathEffect = null

        }

        // 又来画出横向的线条
        // 最后一条不要画,从上往下吧
        mXYTextPaint.textAlign = Paint.Align.LEFT
        for (i in 0 until ySize) {
            if (i != 0) {
                // 画出线
                mPath.reset()
                val yAxis = yOneDistance * (i + 1) + mEndY
                mPath.moveTo(mStartX, yAxis)
                mPath.lineTo(mEndX.toFloat(), yAxis)
                canvas?.drawPath(mPath, mHorizontalLinePaint)
            }
            // 画出文字 Y轴的tip文字颜色不一样
            canvas?.drawText(if (i == 0) {
                mXYTextPaint.color = mYTipTextColor
                mYTextTip
            } else {
                mXYTextPaint.color = mXYTextColor
                mYTextArray[i - 1]
            }, 0f, mEndY + (i + 1) * yOneDistance, mXYTextPaint)
        }

        // 最后再画出分数线条
        canvas?.drawPath(mScoreLinePath, mScorePaint)
        mScorePaint.pathEffect = null
    }

    /**
     * 测量文字画高
     */
    private fun getXYTextBoundRect(str: String): Rect {
        val rect = Rect()
        mXYTextPaint.getTextBounds(str, 0, str.length - 1, rect)
        return rect
    }
}