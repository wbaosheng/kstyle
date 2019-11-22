package com.beta.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.beta.R
import kotlinx.android.synthetic.main.main_layout.*
import reform.ko.log.Logk
import reform.ko.task.JobTask

class KActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        initView(this)
    }

    private fun initView(kActivity: KActivity) {
        bindView(kActivity, button_recyleview)
        bindView(kActivity, button_start_permission)
    }

    private fun bindView(kActivity: KActivity, button: Button) {
        button.setOnClickListener(kActivity)
    }

    override fun onClick(v: View?) {
        when (v) {
            button_recyleview -> {
                val intent = Intent(this, RecyclerActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                this.startActivity(intent)
            }
            button_start_permission -> {
                startBackgroundJob()
            }
            button_exclude_recent_activity -> {
                startActivity(Intent(this, ExcludeRecentActivity::class.java))
            }
        }
    }

    private fun startBackgroundJob(): JobTask {
        Logk.d("ktask", "startBackgroundJob()")
        var out = 1
        return JobTask.build {
        }.next<String, Int>(JobTask.Task) {
            out++
            val a = "a"
            val b = "b"
            val c = a + b
            Logk.d("ktask", "JobTask->Task $c")
            1
        }.next<Int, String>(JobTask.Io) {
            val a = 0
            Logk.d("ktask", "JobTask->Io $a")
            "a"
        }.start()
    }
}

