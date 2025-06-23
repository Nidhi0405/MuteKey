package com.bbm.applock.util

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.graphics.scale
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IPieDataSet
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin


class RoundedSlicesPieChartRenderer(
    chart: PieChart?,
    animator: ChartAnimator?,
    viewPortHandler: ViewPortHandler?
) : PieChartRenderer(chart, animator, viewPortHandler)
{

    init {
        chart?.setDrawRoundedSlices(true)
    }

    override fun drawDataSet(c: Canvas?, dataSet: IPieDataSet) {
        var angle = 0f
        val rotationAngle = mChart.rotationAngle
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        val circleBox = mChart.circleBox
        val entryCount = dataSet.entryCount
        val drawAngles = mChart.drawAngles
        val center = mChart.centerCircleBox
        val radius = mChart.radius
        val drawInnerArc = mChart.isDrawHoleEnabled && !mChart.isDrawSlicesUnderHoleEnabled
        val userInnerRadius = if (drawInnerArc) radius * (mChart.holeRadius / 100f) else 0f
        val roundedRadius = (radius - userInnerRadius) / 2f
        val roundedCircleBox = RectF()

        val pathBuffer = Path()
        val mInnerRectBuffer = RectF()

        val visibleCount = (0 until entryCount).count {
            dataSet.getEntryForIndex(it).y > 0f
        }

        val sliceSpace = if (visibleCount <= 1) 0f else getSliceSpace(dataSet)

        for (j in 0 until entryCount) {
            val e = dataSet.getEntryForIndex(j)
            val sliceAngle = drawAngles[j]
            var innerRadius = userInnerRadius

            if (e.y <= 0f) {
                Log.w("Renderer", "Skipping slice '${e.label}' with y=${e.y}")
                angle += sliceAngle * phaseX
                continue
            }

            Log.d("Renderer", "Rendering '${e.label}' with y=${e.y}")

            if (mChart.needsHighlight(j) && !drawInnerArc) {
                angle += sliceAngle * phaseX
                continue
            }

            val accountForSpacing = sliceSpace > 0f && sliceAngle <= 180f
            mRenderPaint.color = dataSet.getColor(j)

            val spaceAngleOuter = if (visibleCount == 1) 0f else sliceSpace / (Utils.FDEG2RAD * radius)

            val startAngle = rotationAngle + (angle + spaceAngleOuter / 2f) * phaseY
            var sweepAngle = (sliceAngle - spaceAngleOuter) * phaseY
            if (sweepAngle < 0f) sweepAngle = 0f

            pathBuffer.reset()
            val arcStartX = center.x + radius * cos(startAngle * Utils.FDEG2RAD).toFloat()
            val arcStartY = center.y + radius * sin(startAngle * Utils.FDEG2RAD).toFloat()

            if (sweepAngle >= 360f && sweepAngle % 360f <= Utils.FLOAT_EPSILON) {
                pathBuffer.addCircle(center.x, center.y, radius, Path.Direction.CW)
            } else {
                if (drawInnerArc) {
                    val x = center.x + (radius - roundedRadius) * cos(startAngle * Utils.FDEG2RAD)
                    val y = center.y + (radius - roundedRadius) * sin(startAngle * Utils.FDEG2RAD)
                    roundedCircleBox.set(x - roundedRadius, y - roundedRadius, x + roundedRadius, y + roundedRadius)
                    pathBuffer.arcTo(roundedCircleBox, startAngle - 180, 180f)
                }
                pathBuffer.arcTo(circleBox, startAngle, sweepAngle)
            }

            mInnerRectBuffer.set(
                center.x - innerRadius, center.y - innerRadius,
                center.x + innerRadius, center.y + innerRadius
            )

            if (drawInnerArc && (innerRadius > 0f || accountForSpacing)) {
                if (accountForSpacing) {
                    val minRadius = calculateMinimumRadiusForSpacedSlice(
                        center, radius,
                        sliceAngle * phaseY,
                        arcStartX, arcStartY,
                        startAngle,
                        sweepAngle
                    ).let { abs(it) }
                    innerRadius = innerRadius.coerceAtLeast(minRadius)
                }

                val spaceAngleInner = if (visibleCount == 1 || innerRadius == 0f) 0f
                else sliceSpace / (Utils.FDEG2RAD * innerRadius)
                val startAngleInner = rotationAngle + (angle + spaceAngleInner / 2f) * phaseY
                val sweepAngleInner = max(0f, (sliceAngle - spaceAngleInner) * phaseY)
                val endAngleInner = startAngleInner + sweepAngleInner

                if (sweepAngle >= 360f && sweepAngle % 360f <= Utils.FLOAT_EPSILON) {
                    pathBuffer.addCircle(center.x, center.y, innerRadius, Path.Direction.CCW)
                } else {
                    val x = center.x + (radius - roundedRadius) * cos(endAngleInner * Utils.FDEG2RAD)
                    val y = center.y + (radius - roundedRadius) * sin(endAngleInner * Utils.FDEG2RAD)
                    roundedCircleBox.set(x - roundedRadius, y - roundedRadius, x + roundedRadius, y + roundedRadius)
                    pathBuffer.arcTo(roundedCircleBox, endAngleInner, 180f)
                    pathBuffer.arcTo(mInnerRectBuffer, endAngleInner, -sweepAngleInner)
                }
            } else {
                if (sweepAngle % 360f > Utils.FLOAT_EPSILON) {
                    if (accountForSpacing) {
                        val midAngle = startAngle + sweepAngle / 2f
                        val offset = calculateMinimumRadiusForSpacedSlice(
                            center, radius, sliceAngle * phaseY,
                            arcStartX, arcStartY, startAngle, sweepAngle
                        )
                        val endX = center.x + offset * cos(midAngle * Utils.FDEG2RAD).toFloat()
                        val endY = center.y + offset * sin(midAngle * Utils.FDEG2RAD).toFloat()
                        pathBuffer.lineTo(endX, endY)
                    } else {
                        pathBuffer.lineTo(center.x, center.y)
                    }
                }
            }

            pathBuffer.close()
            mBitmapCanvas.drawPath(pathBuffer, mRenderPaint)
            angle += sliceAngle * phaseX
        }

        MPPointF.recycleInstance(center)
    }
    override fun drawValues(c: Canvas) {
        val data = mChart.data ?: return
        val center = mChart.centerCircleBox
        val radius = mChart.radius
        val drawAngles = mChart.drawAngles
        val dataSet = data.getDataSetByIndex(0)
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        val entryCount = dataSet.entryCount
        var angle = mChart.rotationAngle

        for (i in 0 until entryCount) {
            val entry = dataSet.getEntryForIndex(i)
            if (abs(entry.y) < Utils.FLOAT_EPSILON) continue

            val sliceAngle = drawAngles[i]
            val midAngle = angle + sliceAngle / 2f
            val radians = Math.toRadians(midAngle.toDouble())

            // Icon position - just outside the arc
            val iconRadius = radius * 1.2f
            val iconX = center.x + iconRadius * cos(radians).toFloat()
            val iconY = center.y + iconRadius * sin(radians).toFloat()

            val pieEntry = entry as PieEntry
            val iconDrawable = pieEntry.icon ?: continue

            // Convert to Bitmap & scale
            val rawBitmap = (iconDrawable as? BitmapDrawable)?.bitmap ?: continue
            val scaled = rawBitmap.scale(80, 80)

            // Draw icon at calculated position
            c.drawBitmap(scaled, iconX - scaled.width / 2f, iconY - scaled.height / 2f, null)

            // Move angle forward
            angle += sliceAngle * phaseX
        }
    }

}