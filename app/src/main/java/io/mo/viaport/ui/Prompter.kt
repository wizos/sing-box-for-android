package io.mo.viaport.ui

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import io.mo.viaport.helper.mainScope
import kotlinx.coroutines.launch

class Prompter constructor(private val context: Application) {
    private var toastPrompter: Toast? = null
    fun toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        mainScope.launch {
            toastPrompter?.cancel()
            toastPrompter = context.toast(message, duration)
        }
    }

    fun toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT){
        mainScope.launch {
            toastPrompter?.cancel()
            toastPrompter = context.toast(resId, duration)
        }
    }

    fun toast(@StringRes resId: Int, vararg formatArgs: Any, duration: Int = Toast.LENGTH_SHORT){
        mainScope.launch {
            toastPrompter?.cancel()
            toastPrompter = context.toast(context.getString(resId, *formatArgs), duration)
        }
    }
}

/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param resId the message text resource id.
 */
@Suppress("toast on a thread that has not called Looper.prepare()")
inline fun Context.toast(@StringRes resId: Int, duration: Int = Toast.LENGTH_SHORT): Toast = Toast
    .makeText(this, resId, duration)
    .apply {
        show()
    }

/**
 * Display the simple Toast message with the [Toast.LENGTH_SHORT] duration.
 *
 * @param message the message text.
 */
@Suppress("toast on a thread that has not called Looper.prepare()")
inline fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT): Toast = Toast
    .makeText(this, message, duration)
    .apply {
        show()
    }