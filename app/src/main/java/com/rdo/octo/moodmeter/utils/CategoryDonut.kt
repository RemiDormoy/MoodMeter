package com.rdo.octo.moodmeter.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.FloatEvaluator
import android.animation.ValueAnimator
import android.annotation .SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation .ColorInt
import android.support.v4.graphics.ColorUtils
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator

import java.util.ArrayList

class CategoryDonut @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    private var donutPaint: Paint? = null
    private var donutPath: Path? = null
    private var outterCircle: RectF? = null
    private var innerCircle: RectF? = null

    private var smallRadiusThickness: Float = 0.toFloat()
    private var darkenCircle: Rect? = null
    private var darkenThickness: Int = 0
    private var darkenPaint: Paint? = null
    private val currentSliceList = ArrayList<Slice>()
    private val oldSliceList = ArrayList<Slice>()
    private val oldSliceEqList = ArrayList<Slice>()
    private val newSliceList = ArrayList<Slice>()
    private val newSliceEqList = ArrayList<Slice>()
    private var transitionAnimator: ValueAnimator? = null
    private val angleEvaluator = FloatEvaluator()
    private val colorEvaluator = ArgbEvaluator()
    private var transitionInterpolator: Interpolator = LinearInterpolator()

    private var outterRadius: Float = 0.toFloat()
    private var innerRadius: Float = 0.toFloat()
    private var center: PointF? = null

    private var progression = 0f
    private var middleRadius: Float = 0.toFloat()

    private var iconSizePercent: Float = 0.toFloat()
    private var iconSize = 0
    private var iconAlpha = 0
    private var animationListener: AnimationListener? = null
    private var gestureDetector: GestureDetectorCompat? = null
    private var onSliceClickedListener: OnSliceClickedListener? = null
    var selectedSliceIndex = -1
        private set
    private var selectedSlice: Slice? = null

    init {
        init(context, attrs)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val bundle = state as Bundle
        super.onRestoreInstanceState(bundle.getParcelable("parent_state"))
        progression = bundle.getFloat("progression")
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable("parent_state", super.onSaveInstanceState())
        bundle.putFloat("progression", progression)
        return bundle
    }

    fun addItemList(itemList: List<Item>) {
        if (transitionAnimator != null && transitionAnimator!!.isRunning) {
            transitionAnimator!!.cancel()
        }

        transitionInterpolator = FastOutSlowInInterpolator()

        // save old values
        oldSliceList.clear()
        oldSliceEqList.clear()
        oldSliceList.addAll(currentSliceList)

        currentSliceList.clear()
        newSliceList.clear()
        newSliceEqList.clear()
        var total = 0f
        for (item in itemList) {
            total += item.value
        }
        if (itemList.size > 0) {
            var currentAngle = 0f
            var i: Int
            i = 0
            while (i < itemList.size - 1) {
                val item = itemList[i]
                val angleForValue = item.value / total * 360f
                newSliceList
                    .add(
                        Slice(
                            currentAngle, currentAngle + angleForValue, item.color,
                            item.icon
                        )
                    )
                currentAngle += angleForValue
                i++
            }
            val lastItem = itemList[i]
            newSliceList.add(Slice(currentAngle, 360f, lastItem.color, lastItem.icon))
        }

        if (progression == 0f) {
            currentSliceList.addAll(newSliceList)
            oldSliceList.clear()
            newSliceList.clear()
            val progressionAnimator = ValueAnimator.ofFloat(0f, 1f)
            progressionAnimator.duration = 10
            progressionAnimator.addUpdateListener { animation -> updateProgression(animation.animatedValue as Float) }
            val alphaAnimator = ValueAnimator.ofInt(0, 255)
            alphaAnimator.duration = 10
            alphaAnimator.addUpdateListener { animation -> updateIconAlpha(animation.animatedValue as Int) }
            val showAnimator = AnimatorSet()
            showAnimator.playSequentially(progressionAnimator, alphaAnimator)
            showAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animationListener != null) {
                        animationListener!!.onAnimationEnd()
                    }
                }
            })
            showAnimator.start()
        } else {
            if (isInEditMode) {
                currentSliceList.addAll(newSliceList)
                oldSliceList.clear()
                newSliceList.clear()
                invalidate()
                return
            }
            oldSliceEqList.addAll(oldSliceList)
            newSliceEqList.addAll(newSliceList)
            val sliceCountDiff = newSliceList.size - oldSliceList.size

            val fromList = if (sliceCountDiff > 0) oldSliceList else newSliceList
            val toList = if (sliceCountDiff > 0) newSliceList else oldSliceList
            val substractList = if (sliceCountDiff > 0) oldSliceEqList else newSliceEqList
            for (i in fromList.size until toList.size) {
                val newSlice = toList[i]
                substractList.add(Slice(360f, 360f, newSlice.color, newSlice.icon))
            }

            oldSliceList.clear()
            currentSliceList.addAll(oldSliceEqList)
            transitionAnimator = ValueAnimator.ofFloat(0f, 1f)
            transitionAnimator!!.duration = 10
            transitionAnimator!!.addUpdateListener { animation ->
                val fraction = transitionInterpolator.getInterpolation(
                    animation.animatedFraction
                )
                currentSliceList.clear()
                for (i in newSliceEqList.indices) {
                    val oldSlice = oldSliceEqList[i]
                    val newSlice = newSliceEqList[i]
                    currentSliceList.add(
                        i, Slice(
                            angleEvaluator.evaluate(
                                fraction,
                                oldSlice.beginAngle,
                                newSlice.beginAngle
                            )!!,
                            angleEvaluator.evaluate(
                                fraction,
                                oldSlice.endAngle,
                                newSlice.endAngle
                            )!!,
                            colorEvaluator.evaluate(
                                fraction,
                                oldSlice.color,
                                newSlice.color
                            ) as Int,
                            newSlice.icon
                        )
                    )
                }
                invalidate()
            }
            transitionAnimator!!.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationCancel(animation: Animator) {
                    // on cancellation, the current state of the slices is now the final step
                    // the donut will then re-animate from this current state to a new one
                    newSliceList.clear()
                    newSliceList.addAll(currentSliceList)
                }

                override fun onAnimationEnd(animation: Animator) {
                    newSliceEqList.clear()
                    oldSliceEqList.clear()
                    currentSliceList.clear()
                    currentSliceList.addAll(newSliceList)
                    newSliceList.clear()
                    invalidate()

                }
            })
            transitionAnimator!!.start()
            invalidate()
        }
    }

    fun setAnimationListener(listener: AnimationListener) {
        animationListener = listener
    }

    fun setOnSliceClickedListener(onSliceClickedListener: OnSliceClickedListener) {
        this.onSliceClickedListener = onSliceClickedListener
    }

    fun selectSlice(sliceIndex: Int) {
        if (sliceIndex >= 0) {
            this.selectedSliceIndex = sliceIndex
            this.selectedSlice = currentSliceList[sliceIndex]
            invalidate()
        } else {
            unselectSlice()
        }
    }

    @SuppressLint("ClickableViewAccessibility") // it's done in the gestureDetector
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (onSliceClickedListener == null || progression != 1f) {
            false
        } else gestureDetector!!.onTouchEvent(event)
    }

    fun unselectSlice() {
        this.selectedSliceIndex = -1
        this.selectedSlice = null
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

        var width = widthSize
        var height = heightSize

        if (width == 0) {
            width = height
        } else if (height == 0) {
            height = width
        } else {
            val squareSize = Math.min(width, height)
            width = squareSize
            height = squareSize
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        center = PointF(w / 2f, w / 2f)
        updateRingBounds(w)

        darkenPaint!!.strokeWidth = darkenThickness.toFloat()
        darkenCircle!!.set(
            innerCircle!!.left.toInt() - darkenThickness / 2,
            innerCircle!!.top.toInt() - darkenThickness / 2,
            innerCircle!!.right.toInt() + darkenThickness / 2,
            innerCircle!!.bottom.toInt() + darkenThickness / 2
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var iconCenterX: Double
        var iconCenterY: Double
        var midAngle: Double
        var iconLeft: Int
        var iconTop: Int
        var iconRight: Int
        var iconBottom: Int
        for (slice in currentSliceList) {
            drawSlice(
                canvas, donutPaint!!, computeSliceColor(slice), slice.beginAngle,
                slice.endAngle
            )
            midAngle = (slice.endAngle + slice.beginAngle) / 2f * Math.PI / 180.0
            iconCenterX = center!!.x + middleRadius * Math.cos(midAngle)
            iconCenterY = center!!.y + middleRadius * Math.sin(midAngle)
            iconLeft = (iconCenterX - iconSize / 2f).toInt()
            iconTop = (iconCenterY - iconSize / 2f).toInt()
            iconRight = (iconCenterX + iconSize / 2f).toInt()
            iconBottom = (iconCenterY + iconSize / 2f).toInt()
            if (slice.icon != null && iconAlpha > 0
                && isPointInSlice(iconLeft.toFloat(), iconTop.toFloat(), slice)
                && isPointInSlice(iconRight.toFloat(), iconTop.toFloat(), slice)
                && isPointInSlice(iconLeft.toFloat(), iconBottom.toFloat(), slice)
                && isPointInSlice(iconRight.toFloat(), iconBottom.toFloat(), slice)
            ) {
                slice.icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                slice.icon.alpha = iconAlpha
                slice.icon.draw(canvas)
            }
        }
        drawDarkenRing(canvas)
    }

    private fun computeSliceColor(slice: Slice): Int {
        val color = slice.color
        return if (selectedSlice == null || slice === selectedSlice) {
            color
        } else {
            ColorUtils.setAlphaComponent(color, 0x7A)
        }
    }

    private fun updateRingBounds(w: Int) {
        outterRadius = w / 2f
        val smallRadiusOffset = outterRadius * smallRadiusThickness
        val smallDiameter = outterRadius * 2 - smallRadiusOffset
        outterCircle!!.set(
            smallRadiusOffset - progression * smallRadiusOffset,
            smallRadiusOffset - progression * smallRadiusOffset,
            smallDiameter + progression * smallRadiusOffset,
            smallDiameter + progression * smallRadiusOffset
        )

        innerCircle!!.set(
            smallRadiusOffset, smallRadiusOffset,
            smallDiameter, smallDiameter
        )
        innerRadius = innerCircle!!.width() / 2f
        middleRadius = (outterRadius + innerRadius) / 2f
        iconSize = ((outterRadius - innerRadius) / Math.sqrt(2.0) * iconSizePercent).toInt()
    }

    private fun updateProgression(newProgression: Float) {
        progression = newProgression
        updateRingBounds(width)
        invalidate()
    }

    private fun updateIconAlpha(newAlpha: Int) {
        iconAlpha = newAlpha
        invalidate()
    }

    private fun drawDarkenRing(canvas: Canvas) {
        darkenPaint!!.color = Color.argb(50, 0, 0, 0)
        canvas.drawCircle(
            darkenCircle!!.centerX().toFloat(), darkenCircle!!.centerY().toFloat(), darkenCircle!!.width() / 2f,
            darkenPaint!!
        )
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        /*val array = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CategoryDonut,
            0,
            0
        )*/
        smallRadiusThickness = 0.6f
                /*array.getFraction(
            R.styleable.CategoryDonut_hbmc_widthPercent, 1, 1,
            .33f
        )*/
        darkenThickness = 0/*array.getDimensionPixelSize(
            R.styleable.CategoryDonut_hbmc_darkenThickness, 0
        )*/
        iconSizePercent = 1f/*array.getFraction(
            R.styleable.CategoryDonut_hbmc_iconSize, 1, 1, 0f
        )*/
        val isAnimated = false/*array.getBoolean(
            R.styleable.CategoryDonut_hbmc_animate, true
        )*/
        progression = (if (isAnimated) 0 else 1).toFloat()
        iconAlpha = if (isAnimated) 0 else 255
        //array.recycle()

        donutPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        donutPaint!!.color = Color.RED
        donutPaint!!.style = Paint.Style.FILL

        darkenPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        darkenPaint!!.style = Paint.Style.STROKE

        donutPath = Path()
        outterCircle = RectF()
        innerCircle = RectF()
        darkenCircle = Rect()

        if (isInEditMode) {
            progression = 1f
            val itemList = ArrayList<Item>()
            itemList.add(Item(Color.GRAY, 120f, null))
            itemList.add(Item(Color.LTGRAY, 120f, null))
            itemList.add(Item(Color.DKGRAY, 120f, null))
            addItemList(itemList)
        }

        gestureDetector = GestureDetectorCompat(
            context,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    if (onSliceClickedListener == null || progression != 1f) {
                        performClick()
                        return false
                    }
                    return true
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    if (onSliceClickedListener == null || progression != 1f) {
                        performClick()
                        return false
                    }
                    val sliceIndexFromPoint = getSliceIndexFromPoint(e.x, e.y)
                    if (sliceIndexFromPoint != -1) {
                        performClick()
                        onSliceClickedListener!!.onSliceCLicked(sliceIndexFromPoint)
                        return true
                    }
                    return false
                }
            })
    }

    private fun drawSlice(
        c: Canvas,
        p: Paint,
        @ColorInt color: Int,
        startAngle: Float,
        endAngle: Float
    ) {
        p.color = color
        val isFullCircle = endAngle - 360 == startAngle
        if (isFullCircle) {
            val strokeWidth = outterCircle!!.width() / 2 - innerRadius
            p.strokeWidth = strokeWidth
            p.style = Paint.Style.STROKE
            val fullCircleRadius =
                (middleRadius - innerRadius) / (outterRadius - innerRadius) * strokeWidth + innerRadius
            c.drawCircle(center!!.x, center!!.y, fullCircleRadius, p)
        } else {
            p.style = Paint.Style.FILL
            p.strokeWidth = 0f
            val sweepAngle = endAngle - startAngle
            donutPath!!.reset()
            donutPath!!.arcTo(outterCircle, startAngle, sweepAngle, false)
            donutPath!!.arcTo(innerCircle, startAngle + sweepAngle, -sweepAngle, false)
            donutPath!!.close()
            c.drawPath(donutPath!!, p)
        }
    }

    /**
     * Return the slice index for a specific coordinate.
     *
     * @param x the x coordinate, local to the view
     * @param y the y coordinate, local to the view
     * @return the index of the slice or -1 if not in any slice
     */
    private fun getSliceIndexFromPoint(x: Float, y: Float): Int {
        for (i in currentSliceList.indices) {
            val slice = currentSliceList[i]
            if (isPointInSlice(x, y, slice)) {
                return i
            }
        }
        return -1
    }

    private fun isPointInSlice(x: Float, y: Float, slice: Slice): Boolean {
        val xInDonutCoordinate = x - center!!.x
        val yInDonutCoordinate = y - center!!.y
        val pointRadius = Math.sqrt(
            Math.pow(xInDonutCoordinate.toDouble(), 2.0) + Math.pow(yInDonutCoordinate.toDouble(), 2.0)
        )
        // check radius between small and big
        if (pointRadius >= innerRadius && pointRadius <= outterRadius) {

            var rad = Math.atan2(yInDonutCoordinate.toDouble(), xInDonutCoordinate.toDouble())
            if (rad < 0) {
                rad += 2 * Math.PI
            }
            val deg = rad * (180 / Math.PI)
            if (deg >= slice.beginAngle && deg < slice.endAngle) {
                return true
            }
        }
        return false
    }

    interface AnimationListener {
        fun onAnimationEnd()
    }

    interface OnSliceClickedListener {
        fun onSliceCLicked(sliceIndex: Int)
    }

    class Item(
        @param:ColorInt @field:ColorInt @get:ColorInt
        val color: Int, val value: Float, val icon: Drawable?
    ) {

        override fun equals(obj: Any?): Boolean {
            return obj is Item && color == obj.color && icon === obj
                .icon && value == obj.value
        }
    }

    private class Slice constructor(
         val beginAngle: Float,
         val endAngle: Float,
         val color: Int,
         val icon: Drawable?
    ) {

        override fun toString(): String {
            return beginAngle.toString() + " - " + endAngle
        }
    }
}
