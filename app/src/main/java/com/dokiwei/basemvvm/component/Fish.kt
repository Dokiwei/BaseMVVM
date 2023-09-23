package com.dokiwei.basemvvm.component

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View

/**
 * @author DokiWei
 * @date 2023/9/20 16:26
 */


// 定义一个自定义的View类，继承自View，并重写它的onDraw(Canvas canvas)方法，实现自己的绘制逻辑
class FishView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    // 定义一个Paint对象，用来设置画笔的颜色、宽度、样式等属性
    private val mPaint by lazy { Paint() }

    // 定义一个Path对象，用来绘制贝塞尔曲线
    private val mPath: Path

    // 定义一个PointF对象，用来表示鱼的中心点坐标
    private val middlePoint: PointF

    // 定义一个构造方法，传入Context和AttributeSet对象，并初始化Paint对象和Path对象
    init {
        mPaint.setColor(Color.RED) // 设置画笔颜色为红色
        mPaint.strokeWidth = 8f // 设置画笔宽度为8像素
        mPaint.style = Paint.Style.FILL // 设置画笔样式为填充
        mPaint.isAntiAlias = true // 设置画笔抗锯齿为true
        mPath = Path()
        middlePoint = PointF(0f, 0f)
    }

    // 重写onMeasure方法，根据鱼的尺寸和比例，设置自定义View的宽度和高度
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = (6 * HEAD_RADIUS).toInt() // 计算自定义View的宽度，为6倍鱼头半径
        val height = (6 * HEAD_RADIUS).toInt() // 计算自定义View的高度，为6倍鱼头半径
        setMeasuredDimension(width, height) // 设置自定义View的宽度和高度
        middlePoint[width / 2f] = height / 2f // 设置鱼的中心点坐标为自定义View的中心点坐标
    }

    // 重写onDraw方法，在Canvas对象上绘制所需的形状，如圆、椭圆、线、贝塞尔曲线等
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 绘制鱼头，使用canvas.drawCircle方法，传入圆心坐标、半径和画笔对象
        mPaint.alpha = 150
        canvas.drawCircle(middlePoint.x, middlePoint.y, HEAD_RADIUS, mPaint)

        // 绘制鱼身体，使用canvas.drawOval方法，传入椭圆的左上右下坐标和画笔对象
        val bodyLeft = middlePoint.x - BODY_LENGTH
        val bodyTop = middlePoint.y - HEAD_RADIUS
        val bodyRight = middlePoint.x
        val bodyBottom = middlePoint.y + HEAD_RADIUS
        canvas.drawOval(bodyLeft, bodyTop, bodyRight, bodyBottom, mPaint)

        // 绘制鱼尾巴，使用canvas.drawLine方法，传入线的起点和终点坐标和画笔对象
        val tailStartY = middlePoint.y
        val tailEndX = bodyLeft - TAIL_WIDTH
        val tailEndY1 = tailStartY - TAIL_HEIGHT / 2f
        val tailEndY2 = tailStartY + TAIL_HEIGHT / 2f
        canvas.drawLine(bodyLeft, tailStartY, tailEndX, tailEndY1, mPaint)
        canvas.drawLine(bodyLeft, tailStartY, tailEndX, tailEndY2, mPaint)

        // 绘制鱼眼睛，使用canvas.drawCircle方法，传入圆心坐标、半径和画笔对象
        val eyeCenterX = middlePoint.x + 0.6f * HEAD_RADIUS
        val eyeCenterY = middlePoint.y - 0.4f * HEAD_RADIUS
        canvas.drawCircle(eyeCenterX, eyeCenterY, EYE_RADIUS, mPaint)

        // 绘制鱼鳍，使用mPath对象，调用moveTo、quadTo和close方法，绘制二阶贝塞尔曲线，并使用canvas.drawPath方法，传入Path对象和画笔对象
        val finsStartX = middlePoint.x - 0.8f * HEAD_RADIUS
        val finsStartY = middlePoint.y - HEAD_RADIUS
        val finsControlX = finsStartX - 0.5f * FINS_LENGTH
        val finsControlY = finsStartY - 1.2f * FINS_LENGTH
        val finsEndX = finsStartX + 0.4f * FINS_LENGTH
        val finsEndY = finsStartY - 0.6f * FINS_LENGTH
        mPath.reset()
        mPath.moveTo(finsStartX, finsStartY)
        mPath.quadTo(finsControlX, finsControlY, finsEndX, finsEndY)
        mPath.close()
        canvas.drawPath(mPath, mPaint)
    }

    companion object {
        // 定义一些常量，表示鱼的各个部分的尺寸和比例
        private const val HEAD_RADIUS = 100f // 鱼头的半径
        private const val BODY_LENGTH = 3.2f * HEAD_RADIUS // 鱼身体的长度
        private const val FINS_LENGTH = 0.6f * HEAD_RADIUS // 鱼鳍的长度
        private const val TAIL_WIDTH = 1.5f * HEAD_RADIUS // 鱼尾巴的宽度
        private const val TAIL_HEIGHT = 1.8f * HEAD_RADIUS // 鱼尾巴的高度
        private const val EYE_RADIUS = 0.15f * HEAD_RADIUS // 鱼眼睛的半径
    }
}