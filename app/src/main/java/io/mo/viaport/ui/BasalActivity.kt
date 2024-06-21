package io.mo.viaport.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import com.elvishew.xlog.XLog
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

open class BasalActivity : ComponentActivity() {

    private val requestCode = AtomicInteger()
    private val callbackMap = mutableMapOf<Int, (resultCode: Int, data: Intent?) -> Unit>()

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var permissionCallback: (Boolean) -> Unit = {}

    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private var permissionsCallback: (Map<String, Boolean>) -> Unit = {}

    private lateinit var documentLauncher: ActivityResultLauncher<Array<String>>
    private var documentCallback: (Uri?) -> Unit = {}

    // private lateinit var requestInstallAPKPermissionLauncher: ActivityResultLauncher<String>
    // private var requestInstallAPKPermissionCallback: (Boolean) -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 要让setOnApplyWindowInsetsListener()正常工作，您必须确保您的活动处于全屏模式。您可以使用下面的方法来完成此操作
        // 关闭装饰装修系统窗口，这使我们能够处理插入，包括IME动画
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // This also sets up the initial system bar style based on the platform theme
        enableEdgeToEdge()

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionCallback.invoke(it) }

        permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsCallback.invoke(it) }

        // requestInstallAPKPermissionLauncher = registerForActivityResult(requestInstallAppContract(packageManager)) { requestInstallAPKPermissionCallback.invoke(it) }

        documentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()){ documentCallback.invoke(it) }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackMap.remove(requestCode)?.invoke(resultCode, data)
    }
    fun startActivityForResult(intent: Intent, callback: (resultCode: Int, data: Intent?) -> Unit) {
        val code = requestCode.getAndIncrement()
        try {
            startActivityForResult(intent, code)
            callbackMap[code] = callback
        } catch (e: Exception) {
            callback.invoke(Activity.RESULT_CANCELED, null)
        }
    }

    suspend fun requestPermission(permission: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            permissionCallback = { continuation.resume(it) }
            permissionLauncher.launch(permission)
        }
    }

    suspend fun requestPermissions(permissions: Array<String>): Map<String, Boolean> {
        return suspendCancellableCoroutine { continuation ->
            permissionsCallback = { continuation.resume(it) }
            permissionsLauncher.launch(permissions)
        }
    }

    suspend fun openDocument(mimeTypes: Array<String>): Uri? {
        return suspendCancellableCoroutine { continuation ->
            documentCallback = { continuation.resume(it) }
            documentLauncher.launch(mimeTypes)
        }
    }
    // suspend fun requestInstallAPKPermission(): Boolean {
    //     if (packageManager.canRequestPackageInstalls()) return true
    //     return suspendCancellableCoroutine { continuation ->
    //         requestInstallAPKPermissionCallback = { continuation.resume(it) }
    //         requestInstallAPKPermissionLauncher.launch(packageName)
    //     }
    // }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        XLog.d("请求权限回调 B：$requestCode ")
        callbackMap.remove(requestCode)?.invoke(Activity.RESULT_OK, null)
    }
}