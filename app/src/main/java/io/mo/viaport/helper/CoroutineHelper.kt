package io.mo.viaport.helper

import com.elvishew.xlog.XLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren


val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
    XLog.e("$context execute error, thread: ${Thread.currentThread().name} " + throwable)
    throwable.printStackTrace()
}

val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main + exceptionHandler)
val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

fun CoroutineScope.cancelChildren(cause: CancellationException? = null) {
    val job = coroutineContext[Job] ?: error("Scope cannot be cancelled because it does not have a job: $this")
    job.cancelChildren(cause)
}
