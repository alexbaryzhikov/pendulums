package ru.alexb.pendulums

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
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
    private var count = DEFAULT_COUNT
    private var maxWaveLength = DEFAULT_WAVE_LENGTH
    private var timeScale = DEFAULT_TIME_SCALE
    private var waveLengths: List<Double> = emptyList()
    private var verticalOffsets: List<Double> = emptyList()
    private var horizontalOffset: Double = 0.0
    private var radius: Double = 0.0
    private var showFps = BuildConfig.DEBUG
    private var isAnimating = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        frameTicks = Observable.interval(10, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { invalidate() }
        t0 = now()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        frameTicks?.dispose()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updateMeasurements()
    }

    private fun updateMeasurements() {
        waveLengths = linSpace(1.0, maxWaveLength, count)
        radius = height / (waveLengths.size.toDouble() + 1.0) / 2.0 - 5.0
        val step = height / (waveLengths.size.toDouble() + 1.0)
        verticalOffsets = List(waveLengths.size) { (it + 1.0) * step }
        horizontalOffset = radius + 10.0
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        updateTime()
        updateFps()

        for (i in 0..waveLengths.lastIndex) {
            val x = (width - horizontalOffset * 2) * getPosition(waveLengths[i]) + horizontalOffset
            val y = verticalOffsets[i]
            canvas.drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), colors[i % colors.size])
        }

        if (showFps) {
            canvas.drawText(fps.toString(), 5f, 40f, textPaint)
        }
    }

    private fun updateTime() {
        val t1 = now()
        if (isAnimating) {
            time += ((t1 - t0) * timeScale).toLong()
        }
        t0 = t1
    }

    private fun updateFps() {
        val t = now()
        frames.removeAll { it < t - 2000 }
        frames.add(t)
        fps = frames.size * 1000 / (t - frames[0] + 1)
    }

    private fun getPosition(offset: Double) = 0.5 - cos(time * offset / 1000.0) / 2.0

    fun isPlaying(): Boolean {
        return isAnimating
    }

    fun getTime(): Long {
        return time
    }

    fun setTime(time: Long) {
        this.time = time
    }

    fun setCount(count: Int) {
        this.count = count
        updateMeasurements()
    }

    fun setMaxWaveLength(maxWaveLength: Double) {
        this.maxWaveLength = maxWaveLength
        waveLengths = linSpace(1.0, maxWaveLength, count)
        updateMeasurements()
    }

    fun setTimeScale(timeScale: Double) {
        this.timeScale = timeScale
    }

    fun resetAnimation() {
        t0 = now()
        time = 0
        isAnimating = true
    }

    fun toggleAnimation() {
        isAnimating = !isAnimating
    }

    companion object {
        const val DEFAULT_COUNT = 12
        const val DEFAULT_WAVE_LENGTH = 1.5
        const val DEFAULT_TIME_SCALE = 1.0

        fun now(): Long = System.currentTimeMillis()

        fun linSpace(start: Double, end: Double, num: Int): List<Double> {
            val delta = end - start
            val step = delta / (num - 1)
            return List(num) { start + it * step }
        }
    }
}
