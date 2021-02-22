package ru.alexb.pendulums

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

@Suppress("UNUSED_PARAMETER")
class MainActivity : AppCompatActivity() {
    private lateinit var canvasView: CanvasView
    private lateinit var toggleAnimationButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canvasView = findViewById(R.id.canvas_view)
        toggleAnimationButton = findViewById(R.id.toggle_animation_button)
        findViewById<EditText>(R.id.number_edit_text).setOnEditorActionListener(::onEditorAction)
    }

    private fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?) = when (actionId) {
        EditorInfo.IME_ACTION_DONE -> {
            v.clearFocus()
            hideIme(v)
            onNumberEntered(v.text)
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
        val default = 12
        val n = when {
            s == null || s.isBlank() -> default
            else -> {
                val input = try {
                    Integer.parseInt(s.toString())
                } catch (e: Throwable) {
                    default
                }
                if (input > 1) {
                    input.coerceAtMost(1000)
                } else {
                    default
                }
            }
        }
        canvasView.setNumber(n)
        updateToggleAnimationButton()
    }

    private fun updateToggleAnimationButton() {
        toggleAnimationButton.text = when (canvasView.isPlaying()) {
            true -> getText(R.string.pause)
            else -> getText(R.string.play)
        }
    }

    fun onResetAnimationClick(v: View) {
        canvasView.resetAnimation()
        updateToggleAnimationButton()
    }

    fun onToggleAnimationClick(v: View) {
        canvasView.toggleAnimation()
        updateToggleAnimationButton()
    }

    fun onToggleFpsClick(v: View) {
        canvasView.toggleFps()
    }
}