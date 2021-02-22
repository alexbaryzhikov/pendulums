package ru.alexb.pendulums

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
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
    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 40f
        val colorValue = TypedValue()
        context.theme.resolveAttribute(R.attr.textColorCanvas, colorValue, true)
        color = colorValue.data
    }
    private val purple = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("purple")
    }
    private var frameTicks: Disposable? = null
    private val frames: ArrayList<Long> = ArrayList()
    private var fps: Long = 0
    private var t0: Long = 0
    private var time: Long = 0
    private var isAnimating = false

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
        Log.d(TAG, "w=$w, h=$h")
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null) return
        updateTime()
        updateFps()

        val x = 0.5 - cos(time / 1000.0) / 2.0
        canvas.drawCircle(((width - 200) * x + 100).toFloat(), height / 2.0f, 51f, purple)

        canvas.drawText(fps.toString(), 60f, 100f, textPaint)
    }

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

    fun startAnimate() {
        isAnimating = true
    }

    fun stopAnimate() {
        isAnimating = false
    }

    companion object {
        private const val TAG = "CustomView"

        private fun now(): Long = System.currentTimeMillis()
    }
}
