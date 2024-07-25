package io.mo.viaport.ktx

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.RoundingMode
import java.text.DecimalFormat

fun File.formattedFileSize(): String {
    return length().toDouble().formattedFileSize()
}
fun Long.formattedFileSize(): String {
    return toDouble().formattedFileSize()
}
fun Double.formattedFileSize(): String {
    var fileSize = this / 1024.0
    var fileSizeName = "KB"
    if (fileSize > 1024.0) {
        fileSize /= 1024.0
        fileSizeName = "MB"
        if (fileSize > 1024.0) {
            fileSize /= 1024.0
            fileSizeName = "GB"
        }
    }
    val roundingSize = DecimalFormat("#.##").apply {
        roundingMode = RoundingMode.DOWN
    }
    return "${roundingSize.format(fileSize)} $fileSizeName"
}

private fun File.ensureFile(deleteExistFile: Boolean = false): Boolean {
    if (this.exists()) {
        if (this.isFile){
            if (deleteExistFile){
                this.delete()
            }else{
                return true
            }
        }else{
            if (deleteExistFile){
                this.delete()
            }else{
                return false
            }
        }
    }else{
        this.parentFile?.also { parentFile ->
            if (!parentFile.exists()) {
                parentFile.mkdirs()
            }else if (parentFile.isFile){
                return false
            }
        }
    }

    return try {
        this.createNewFile()
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

// @Throws(IOException::class)
// fun File.copyTo(destFile: File, deleteExistFile: Boolean = true): Boolean {
//     if (!exists()) throw FileNotFoundException("src file not exist")
//
//     if (destFile.exists() && !deleteExistFile) {
//         return false
//     }
//     destFile.createNewFile()
//
//     val srcChannel: FileChannel = FileInputStream(this).channel
//     val dstChannel: FileChannel = FileOutputStream(destFile).channel
//     srcChannel.transferTo(0, srcChannel.size(), dstChannel)
//     srcChannel.close()
//     dstChannel.close()
//     return true
// }
fun File.readBitmap(): Bitmap? {
    return try {
        val fis = FileInputStream(this)
        val bitmap = BitmapFactory.decodeStream(fis)
        fis.close()
        bitmap
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
fun File.save(bmp: Bitmap, deleteExistFile: Boolean = false): File? {
    if (!this.ensureFile(deleteExistFile)) {
        return null
    }
    try {
        val fos = FileOutputStream(this)
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
    return this
}
fun File.save(bytes: ByteArray, deleteExistFile: Boolean = false, append: Boolean = false): Boolean {
    if (!this.ensureFile(deleteExistFile)) {
        return false
    }
    return write(bytes, append)
}
fun File.save(content: String, deleteExistFile: Boolean = false, append: Boolean = false): Boolean {
    if (!this.ensureFile(deleteExistFile)) {
        return false
    }
    return write(content.toByteArray(), append)
}
/**
 * 1. 如果目标是文件
 * 1.1. 是否删除目标文件
 * 1.1.1. 删除，返回 true
 * 1.1.2. 不删，是否需要追加
 * 1.1.2.1. 不追加，返回 false
 * 1.1.2.2. 追加，返回 true
 * 2. 如果目标是目录
 * 1.1. 是否删除目标目录
 * 2.1.1. 删除，返回 true
 * 2.1.2. 不删，返回 false
 */
private fun File.write(bytes: ByteArray, append: Boolean = false): Boolean {
    return try {
        val fos = FileOutputStream(this, append)
        fos.write(bytes)
        fos.flush()
        fos.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}



fun File.readText(): String? {
    return FileInputStream(this).readText()
}

fun InputStream.readText(): String? {
    try {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var count: Int
        while (this.read(buffer).also { count = it } != -1) {
            out.write(buffer, 0, count)
        }
        out.close()
        this.close()
        return out.toString()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}


fun String.toFile(): File? {
    var path = if (this.startsWith("file://")){
        this.substring(7)
    }else{
        this
    }
    val index = path.indexOf("?")
    if (index > 0){
        path = path.substring(0, index)
    }
    val file = File(path)
    // XLog.d("文件：" + file.isFile + ", " + file.path)
    if (file.isFile){
        return file
    }
    return null
}

fun File.tryRenameTo(destFileName: String): Boolean {
    // XLog.i("文件是否存在：" + srcFile.exists() + destFileName);
    val destFile = File(destFileName)
    destFile.parentFile?.let {
        if (!it.exists()){it.mkdirs()}
    }
    return this.renameTo(destFile)
}