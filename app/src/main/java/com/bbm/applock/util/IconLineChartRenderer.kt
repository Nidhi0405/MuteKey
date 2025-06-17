package com.bbm.applock.util
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
                    (px.x + iconsOffset.x - 20).toInt(),
                    (px.y + iconsOffset.y - 20).toInt(),
                    (px.x + iconsOffset.x + 20).toInt(),
                    (px.y + iconsOffset.y + 20).toInt()
                )
                icon.draw(c)
            }
        }
    }
}