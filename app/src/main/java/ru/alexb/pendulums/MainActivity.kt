package ru.alexb.pendulums

import android.os.Bundle
import android.util.Log
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
        onNumberEntered(countText.text)
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
                countText -> onNumberEntered(v.text)
                waveLengthText -> Log.d("MainActivity", "wave length entered")
                timeScaleText -> Log.d("MainActivity", "time scale entered")
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

    private fun onNumberEntered(s: CharSequence?) {
        val n = when {
            s == null || s.isBlank() -> CanvasView.DEFAULT_NUMBER
            else -> {
                val input = try {
                    Integer.parseInt(s.toString())
                } catch (e: Throwable) {
                    CanvasView.DEFAULT_NUMBER
                }
                if (input > 1) {
                    input.coerceAtMost(1000)
                } else {
                    CanvasView.DEFAULT_NUMBER
                }
            }
        }
        canvasView.setNumber(n)
        updateToggleAnimationButton()
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