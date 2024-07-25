package io.mo.viaport.ktx

import com.elvishew.xlog.XLog

fun Throwable.echoStackTrace() {
    XLog.e("$javaClass - $message")
    val stackElements = stackTrace
    for (stackElement in stackElements) {
        XLog.e(stackElement.className + "_" + stackElement.fileName + "_" + stackElement.lineNumber + "_" + stackElement.methodName)
    }
}

val Throwable.readableMessage
    get() = localizedMessage.takeIf { !it.isNullOrBlank() } ?: javaClass.simpleName
