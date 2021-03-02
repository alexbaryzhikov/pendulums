package ru.alexb.pendulums

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {
    private lateinit var canvasView: CanvasView
    private lateinit var toggleAnimationButton: ImageButton
    private lateinit var countText: EditText
    private lateinit var waveLengthText: EditText
    private lateinit var timeScaleText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canvasView = findViewById(R.id.canvas_view)
        toggleAnimationButton = findViewById(R.id.toggle_animation_button)
        countText = findViewById(R.id.count_text)
        countText.setOnEditorActionListener(::onEditorAction)
        waveLengthText = findViewById(R.id.wave_length_text)
        waveLengthText.setOnEditorActionListener(::onEditorAction)
        timeScaleText = findViewById(R.id.time_scale_text)
        timeScaleText.setOnEditorActionListener(::onEditorAction)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_PLAYING_KEY, canvasView.isPlaying())
        outState.putLong(TIME_KEY, canvasView.getTime())
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        onCountEntered(countText.text)
        onWaveLengthEntered(waveLengthText.text)
        onTimeScaleEntered(timeScaleText.text)
        canvasView.setTime(savedInstanceState.getLong(TIME_KEY, 0))
        val isPlaying = savedInstanceState.getBoolean(IS_PLAYING_KEY, true)
        if (canvasView.isPlaying() != isPlaying) {
            canvasView.toggleAnimation()
        }
        updateToggleAnimationButton()
    }

    private fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?) = when (actionId) {
        EditorInfo.IME_ACTION_DONE -> {
            v.clearFocus()
            hideIme(v)
            when (v) {
                countText -> onCountEntered(v.text)
                waveLengthText -> onWaveLengthEntered(v.text)
                timeScaleText -> onTimeScaleEntered(v.text)
            }
            true
        }
        else -> {
            false
        }
    }

    private fun hideIme(v: TextView) {
        val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    }

    private fun onCountEntered(s: CharSequence?) {
        val count = when {
            s == null || s.isBlank() -> CanvasView.DEFAULT_COUNT
            else -> {
                val input = try {
                    s.toString().toInt()
                } catch (e: Throwable) {
                    CanvasView.DEFAULT_COUNT
                }
                input.coerceAtLeast(1).coerceAtMost(1000)
            }
        }
        canvasView.setCount(count)
        updateToggleAnimationButton()
    }

    private fun onWaveLengthEntered(s: CharSequence?) {
        val waveLength = when {
            s == null || s.isBlank() -> CanvasView.DEFAULT_WAVE_LENGTH
            else -> {
                val input = try {
                    s.toString().toDouble()
                } catch (e: Throwable) {
                    CanvasView.DEFAULT_WAVE_LENGTH
                }
                input.coerceAtLeast(1.0).coerceAtMost(1000.0)
            }
        }
        canvasView.setMaxWaveLength(waveLength)
    }

    private fun onTimeScaleEntered(s: CharSequence?) {
        val timeScale = when {
            s == null || s.isBlank() -> CanvasView.DEFAULT_TIME_SCALE
            else -> {
                val input = try {
                    s.toString().toDouble()
                } catch (e: Throwable) {
                    CanvasView.DEFAULT_TIME_SCALE
                }
                input.coerceAtLeast(0.001).coerceAtMost(1000000.0)
            }
        }
        canvasView.setTimeScale(timeScale)
    }

    private fun updateToggleAnimationButton() {
        toggleAnimationButton.setImageResource(
            when (canvasView.isPlaying()) {
                true -> R.drawable.ic_pause
                else -> R.drawable.ic_play
            }
        )
    }

    fun onResetAnimationClick(v: View) {
        canvasView.resetAnimation()
        updateToggleAnimationButton()
    }

    fun onToggleAnimationClick(v: View) {
        canvasView.toggleAnimation()
        updateToggleAnimationButton()
    }

    companion object {
        private const val TIME_KEY = "time"
        private const val IS_PLAYING_KEY = "is_playing"
    }
}