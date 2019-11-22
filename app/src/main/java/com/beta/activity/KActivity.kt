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
                var job = startBackgroundJob()
                job.cancel()
            }
            button_exclude_recent_activity -> {
                startActivity(Intent(this, ExcludeRecentActivity::class.java))
            }
        }
    }

    private fun startBackgroundJob(): JobTask {
        Logk.d(msg = "startBackgroundJob")
        return JobTask.build {
        }.next<String, Int>(JobTask.Logic) {
            Logk.d(msg = "JobTask->Logic")
            1
        }.next<Int, String>(JobTask.Io) {
            Logk.d(msg = "JobTask->Io")
            for (i in 1..10) {
                Logk.d(msg = "JobTask->Io i=$i")
                Thread.sleep(1000)
            }
            "a"
        }.next<String, String>(JobTask.Task) {
            Logk.d(msg = "JobTask->Task")
            "a"
        }.start()
    }
}

