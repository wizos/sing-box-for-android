package io.mo.viaport.ktx

import android.content.Context
import android.os.Environment
import java.io.File


/**
 * 专属的缓存目录
 */
fun Context.exclusiveCacheDir(): File {
    // Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED 检查外部存储是否被挂载并且可用
    // Environment.isExternalStorageRemovable() 检查外部存储是否可移除的方法
    return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable()) {
        this.externalCacheDir ?: this.cacheDir
    } else {
        this.cacheDir
    }
}

/**
 * 专属的文件目录
 */
fun Context.exclusiveFilesDir(): File {
    return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable()) {
        this.getExternalFilesDir(null) ?: this.filesDir
    } else {
        this.filesDir
    }
}