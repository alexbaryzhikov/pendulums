package ru.alexb.pendulums

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.math.cos

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val colors: List<Paint> = listOf(
        R.color.color_01,
        R.color.color_02,
        R.color.color_03,
        R.color.color_04,
        R.color.color_05,
        R.color.color_06,
        R.color.color_07,
        R.color.color_08,
        R.color.color_09,
        R.color.color_10,
        R.color.color_11,
        R.color.color_12,
        R.color.color_13,
        R.color.color_14,
        R.color.color_15,
        R.color.color_16
    ).map { colorId ->
        Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, colorId)
        }
    }
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 40f
        val colorValue = TypedValue()
        context.theme.resolveAttribute(R.attr.textColorCanvas, colorValue, true)
        color = colorValue.data
    }
    private var frameTicks: Disposable? = null
    private val frames: ArrayList<Long> = ArrayList()
    private var fps: Long = 0
    private var t0: Long = 0
    private var time: Long = 0
    private var n = 12
    private var periodMultipliers: List<Double> = emptyList()
    private var verticalOffsets: List<Double> = emptyList()
    private var horizontalOffset: Double = 0.0
    private var r: Double = 0.0
    private var showFps = false
    private var isAnimating = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        frameTicks = Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { invalidate() }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        frameTicks?.dispose()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        Log.d(TAG, "w=$w, h=$h")
        initScene()
    }

    private fun initScene() {
        resetAnimation()
        periodMultipliers = linSpace(1.0, 1.5, n)
        r = height / (periodMultipliers.size.toDouble() + 1.0) / 2.0 - 5.0
        val step = height / (periodMultipliers.size.toDouble() + 1.0)
        verticalOffsets = List(periodMultipliers.size) { (it + 1.0) * step }
        horizontalOffset = r + 10.0
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        updateTime()
        updateFps()

        for (i in 0..periodMultipliers.lastIndex) {
            val x = (width - horizontalOffset * 2) * getPosition(periodMultipliers[i]) + horizontalOffset
            val y = verticalOffsets[i]
            canvas.drawCircle(x.toFloat(), y.toFloat(), r.toFloat(), colors[i % colors.size])
        }

        if (showFps) {
            canvas.drawText(fps.toString(), 5f, 40f, textPaint)
        }
    }

    private fun getPosition(offset: Double) = 0.5 - cos(time * offset / 1000.0) / 2.0

    private fun updateTime() {
        val t1 = now()
        if (isAnimating) {
            time += t1 - t0
        }
        t0 = t1
    }

    private fun updateFps() {
        val t = now()
        frames.removeAll { it < t - 2000 }
        frames.add(t)
        fps = frames.size * 1000 / (t - frames[0] + 1)
    }

    fun setNumber(n: Int) {
        Log.d(TAG, "n=$n")
        this.n = n
        initScene()
    }

    fun resetAnimation() {
        isAnimating = false
        t0 = now()
        time = 0
    }

    fun toggleAnimation() {
        isAnimating = !isAnimating
    }

    fun toggleFps() {
        showFps = !showFps
    }

    companion object {
        private const val TAG = "CustomView"

        fun now(): Long = System.currentTimeMillis()

        fun linSpace(start: Double, end: Double, num: Int): List<Double> {
            val delta = end - start
            val step = delta / (num - 1)
            return List(num) { start + it * step }
        }
    }
}
