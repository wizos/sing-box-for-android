package io.mo.viaport.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class RestartActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 关闭当前 Activity，并立即重新启动应用程序
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }
}

