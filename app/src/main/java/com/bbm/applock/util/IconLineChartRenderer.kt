package com.bbm.applock.util
import android.content.res.Resources
import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class IconLineChartRenderer(
    chart: LineChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : LineChartRenderer(chart, animator, viewPortHandler) {

    private val iconSizeDp = 30f
    private val density = Resources.getSystem().displayMetrics.density
    private val iconSizePx = (iconSizeDp * density).toInt()
    private val halfIconSizePx = iconSizePx / 2

    override fun drawValues(c: Canvas) {
        val lineData = mChart.lineData ?: return
        lineData.dataSets.forEachIndexed { dataSetIndex, dataSet ->
            if (!shouldDrawValues(dataSet)) return@forEachIndexed

            val trans = mChart.getTransformer(dataSet.axisDependency)
            val phaseY = mAnimator.phaseY

            mXBounds.set(mChart, dataSet)
            val iconsOffset = dataSet.iconsOffset

            for (j in mXBounds.min until mXBounds.min + mXBounds.range) {
                val e = dataSet.getEntryForIndex(j)
                val icon = e.icon ?: continue
                val px = trans.getPixelForValues(e.x, e.y * phaseY)

                icon.setBounds(
                    (px.x + iconsOffset.x - halfIconSizePx).toInt(),
                    (px.y + iconsOffset.y - halfIconSizePx).toInt(),
                    (px.x + iconsOffset.x + halfIconSizePx).toInt(),
                    (px.y + iconsOffset.y + halfIconSizePx).toInt()
                )
                icon.draw(c)
            }
        }
    }
}
