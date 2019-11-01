package com.beta.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.beta.R

class LoadMoreView : FrameLayout {
    var progressBar: ProgressBar? = null
        private set
    var textView: TextView? = null
        private set

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.load_more_view, this, true)

        progressBar = findViewById(R.id.pb_load_more_progress)
        textView = findViewById(R.id.tv_load_more_tip)
    }

    fun setText(tip: String) {
        if (textView != null) {
            textView!!.text = tip
        }
    }
}
