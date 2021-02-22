package ru.alexb.pendulums

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var canvasView: CanvasView
    private lateinit var numberEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canvasView = findViewById(R.id.canvas_view)
        numberEditText = findViewById(R.id.number_edit_text)
        numberEditText.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
                hideIme(v)
                onNumberEntered(v.text)
                true
            } else {
                false
            }
        }
    }

    private fun hideIme(v: TextView) {
        val imm: InputMethodManager = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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
    }

    fun onResetAnimationClick(v: View) {
        canvasView.resetAnimation()
    }

    fun onToggleAnimationClick(v: View) {
        canvasView.toggleAnimation()
    }

    fun onToggleFpsClick(v: View) {
        canvasView.toggleFps()
    }
}