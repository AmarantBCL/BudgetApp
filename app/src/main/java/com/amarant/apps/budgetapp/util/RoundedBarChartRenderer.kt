package com.amarant.apps.budgetapp.util

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    private val radius: Float
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val mBarRect = RectF()

    override fun drawDataSet(c: Canvas, dataSet: com.github.mikephil.charting.interfaces.datasets.IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        
        mRenderPaint.color = dataSet.color
        mRenderPaint.style = android.graphics.Paint.Style.FILL
        
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        // Ensure buffers are initialized
        if (mBarBuffers == null || mBarBuffers.size <= index) {
            initBuffers()
        }

        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)

        buffer.feed(dataSet)

        trans.pointValuesToPixel(buffer.buffer)

        val isSingleColor = dataSet.colors.size == 1

        var j = 0
        while (j < buffer.size()) {
            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
                j += 4
                continue
            }

            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break

            if (!isSingleColor) {
                mRenderPaint.color = dataSet.getColor(j / 4)
            }

            val left = buffer.buffer[j]
            val top = buffer.buffer[j + 1]
            val right = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            if (dataSet.gradientColor != null) {
                val gradientColor = dataSet.gradientColor
                mRenderPaint.shader = android.graphics.LinearGradient(
                    left, top, left, bottom,
                    gradientColor.startColor,
                    gradientColor.endColor,
                    android.graphics.Shader.TileMode.CLAMP
                )
            } else {
                mRenderPaint.shader = null
            }

            val path = Path()
            
            // Only draw if bar has height
            if (top != bottom) {
                path.addRoundRect(
                    RectF(left, top, right, bottom),
                    floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f),
                    Path.Direction.CW
                )
                c.drawPath(path, mRenderPaint)
            }
            j += 4
        }
    }
}
