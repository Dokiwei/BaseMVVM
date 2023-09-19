package com.dokiwei.basemvvm.component

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PointF
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author DokiWei
 * @date 2023/9/15 22:31
 */
class Fish : Drawable() {
    companion object {
        private const val OTHER_ALPHA = 110
        private const val BODY_ALPHA = 160

        private const val MAIN_RADIUS = 10f

        private const val BODY_LENGTH = 3.2f * MAIN_RADIUS

        private const val FIND_FINS_LENGTH = 0.9f * MAIN_RADIUS
        private const val FINS_LENGTH = 1.3f * MAIN_RADIUS

        private const val BIG_CIRCLE_RADIUS = 0.7f * MAIN_RADIUS
        private const val MIDDLE_CIRCLE_RADIUS = 0.6f * BIG_CIRCLE_RADIUS
        private const val SMALL_CIRCLE_RADIUS = 0.4f * MIDDLE_CIRCLE_RADIUS
        private const val FIND_MIDDLE_CIRCLE_LENGTH = BIG_CIRCLE_RADIUS + MIDDLE_CIRCLE_RADIUS
        private const val FIND_SMALL_CIRCLE_LENGTH = MIDDLE_CIRCLE_RADIUS * (0.4f + 2.7f)
        private const val FIND_TRIANGLE_LENGTH = MIDDLE_CIRCLE_RADIUS * 2.7f
    }

    private val fishMainAngle by lazy { 0f }
    private val middlePoint by lazy {
        PointF(
            4.19f * MAIN_RADIUS,
            4.19f * MAIN_RADIUS
        )
    }

    private val mPath by lazy { Path() }
    private val mPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            setARGB(OTHER_ALPHA, 244, 92, 71)
            isAntiAlias = true
            isDither = true
        }
    }

    private fun calculatePoint(startPoint: PointF, length: Float, angle: Float): PointF {
        val deltaX = (cos(Math.toRadians(angle.toDouble()) * length)).toFloat()
        val deltaY = (sin(Math.toRadians((angle - 180).toDouble()) * length)).toFloat()
        return PointF(startPoint.x + deltaX, startPoint.y + deltaY)
    }


    private fun makeFins(
        canvas: Canvas,
        startPoint: PointF,
        fishAngle: Float,
        isRightFins: Boolean
    ) {
        val controlAngle = 115f
        val endPoint = calculatePoint(startPoint, FINS_LENGTH, fishAngle - 180)
        val controlPoint = calculatePoint(
            startPoint,
            FINS_LENGTH,
            if (isRightFins) fishAngle - controlAngle else fishAngle + controlAngle
        )
        mPath.reset()
        mPath.moveTo(startPoint.x, startPoint.y)
        mPath.quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
        canvas.drawPath(mPath, mPaint)
    }

    override fun draw(canvas: Canvas) {
        val fishAngle = fishMainAngle
        val headPoint = calculatePoint(middlePoint, BODY_LENGTH / 2, fishAngle)
        canvas.drawCircle(headPoint.x, headPoint.y, MAIN_RADIUS, mPaint)

        val finsRightPoint = calculatePoint(headPoint, FIND_FINS_LENGTH, fishAngle - 110)
        makeFins(canvas, finsRightPoint, fishAngle, true)
        canvas.drawCircle(finsRightPoint.x, finsRightPoint.y, MAIN_RADIUS, mPaint)

        val finsLeftPoint = calculatePoint(headPoint, FIND_FINS_LENGTH, fishAngle + 110)
        makeFins(canvas, finsLeftPoint, fishAngle, false)
        canvas.drawCircle(finsLeftPoint.x, finsLeftPoint.y, MAIN_RADIUS, mPaint)

    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return when (mPaint.alpha) {
            0 -> PixelFormat.TRANSPARENT
            255 -> PixelFormat.TRANSLUCENT
            else -> PixelFormat.OPAQUE
        }
    }

    override fun getIntrinsicHeight(): Int {
        return (4.19f * 2 * MAIN_RADIUS).toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return (4.19f * 2 * MAIN_RADIUS).toInt()
    }


}