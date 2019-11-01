package com.beta.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.beta.R
import kotlinx.android.synthetic.main.main_layout.*

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

    private fun bindView(kActivity: KActivity, button: Button?) {
        button!!.setOnClickListener(kActivity)
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
                val intent = Intent()
                // vivo
//                intent.action = "permission.intent.action.softPermissionDetail"
//                intent.setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity")
//                intent.putExtra("packagename", packageName)


                // miui
                intent.action = "miui.intent.action.APP_PERM_EDITOR_PRIVATE"
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                intent.putExtra("extra_pkgname", packageName)
                startActivity(intent)
            }
            button_exclude_recent_activity -> {
                startActivity(Intent(this, ExcludeRecentActivity::class.java))
            }
        }
    }
}

