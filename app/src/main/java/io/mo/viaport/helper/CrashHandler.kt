/******************************************************************************
 * Copyright (C) 2021 by nekohasekai <contact-git@sekai.icu>                  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.mo.viaport.helper

import android.annotation.SuppressLint
import android.os.Build
import com.elvishew.xlog.XLog
import io.nekohasekai.sfa.App
import io.nekohasekai.sfa.BuildConfig
import io.mo.viaport.ktx.exclusiveCacheDir
import io.nekohasekai.sfa.ktx.restart
import io.mo.viaport.utils.TimeUtils
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.regex.Pattern

class CrashHandler: Thread.UncaughtExceptionHandler {
    // @Suppress("UNNECESSARY_SAFE_CALL")
    override fun uncaughtException(thread: Thread, e: Throwable) {
        XLog.e("线程意外报错：$thread")
        e.printStackTrace()

        val logFile = File.createTempFile("${BuildConfig.applicationName} Crash Report " + TimeUtils.format("yyyyMMdd-HHmmss", System.currentTimeMillis()), ".log",
            File(App.instance.exclusiveCacheDir(), "log").also { it.mkdirs() }
        )

        var report = buildReportHeader()

        report += "\n"
        report += "Thread: $thread\n\n"

        report += formatThrowable(e) + "\n\n"

        report += "Logcat: \n\n"

        logFile.writeText(report)

        // try {
        //     Runtime.getRuntime().exec(arrayOf("logcat", "-d")).inputStream.use(
        //         FileOutputStream(
        //             logFile, true
        //         )
        //     )
        // } catch (e: IOException) {
        //     XLog.w(e)
        //     logFile.appendText("Export logcat error: " + formatThrowable(e))
        // }

        val text = logFile.readText()
        XLog.d(text)

        App.instance.restart()
    }

    private fun formatThrowable(throwable: Throwable): String {
        var format = throwable.javaClass.name
        val message = throwable.message
        if (!message.isNullOrBlank()) {
            format += ": $message"
        }
        format += "\n"

        format += throwable.stackTrace.joinToString("\n") {
            "    at ${it.className}.${it.methodName}(${it.fileName}:${if (it.isNativeMethod) "native" else it.lineNumber})"
        }

        val cause = throwable.cause
        if (cause != null) {
            format += "\n\nCaused by: " + formatThrowable(cause)
        }

        return format
    }

    private fun buildReportHeader(): String {
        var report = ""
        report += "Loread ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE}) ${BuildConfig.FLAVOR.uppercase()}\n"
        report += "Date: ${getCurrentMilliSecondUTCTimeStamp()}\n\n"
        report += "OS VERSION: ${getSystemPropertyWithAndroidAPI("os.version")}\n"
        report += "SDK INT: ${Build.VERSION.SDK_INT}\n"
        report += if ("REL" == Build.VERSION.CODENAME) {
            "RELEASE: ${Build.VERSION.RELEASE}"
        } else {
            "CODENAME: ${Build.VERSION.CODENAME}"
        } + "\n"
        report += "ID: ${Build.ID}\n"
        report += "DISPLAY: ${Build.DISPLAY}\n"
        report += "INCREMENTAL: ${Build.VERSION.INCREMENTAL}\n"

        val systemProperties = getSystemProperties()

        report += "SECURITY_PATCH: ${systemProperties.getProperty("ro.build.version.security_patch")}\n"
        report += "IS_DEBUGGABLE: ${systemProperties.getProperty("ro.debuggable")}\n"
        report += "IS_EMULATOR: ${systemProperties.getProperty("ro.boot.qemu")}\n"
        report += "IS_TREBLE_ENABLED: ${systemProperties.getProperty("ro.treble.enabled")}\n"

        report += "TYPE: ${Build.TYPE}\n"
        report += "TAGS: ${Build.TAGS}\n\n"

        report += "MANUFACTURER: ${Build.MANUFACTURER}\n"
        report += "BRAND: ${Build.BRAND}\n"
        report += "MODEL: ${Build.MODEL}\n"
        report += "PRODUCT: ${Build.PRODUCT}\n"
        report += "BOARD: ${Build.BOARD}\n"
        report += "HARDWARE: ${Build.HARDWARE}\n"
        report += "DEVICE: ${Build.DEVICE}\n"
        report += "SUPPORTED_ABIS: ${
            Build.SUPPORTED_ABIS.filter { it.isNotBlank() }.joinToString(", ")
        }\n\n"

        report += "\n\n"

        return report
    }

    private fun getSystemProperties(): Properties {
        val systemProperties = Properties()

        // getprop commands returns values in the format `[key]: [value]`
        // Regex matches string starting with a literal `[`,
        // followed by one or more characters that do not match a closing square bracket as the key,
        // followed by a literal `]: [`,
        // followed by one or more characters as the value,
        // followed by string ending with literal `]`
        // multiline values will be ignored
        val propertiesPattern = Pattern.compile("^\\[([^]]+)]: \\[(.+)]$")
        try {
            val process = ProcessBuilder().command("/system/bin/getprop")
                .redirectErrorStream(true)
                .start()
            val inputStream = process.inputStream
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            var key: String?
            var value: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                val matcher = propertiesPattern.matcher(line.toString())
                if (matcher.matches()) {
                    key = matcher.group(1)
                    value = matcher.group(2)
                    if (key != null && value != null && key.isNotEmpty() && value.isNotEmpty()) systemProperties[key] = value
                }
            }
            bufferedReader.close()
            process.destroy()
        } catch (e: IOException) {
            XLog.e("Failed to get run \"/system/bin/getprop\" to get system properties. $e")
        }

        //for (String key : systemProperties.stringPropertyNames()) {
        //    Logger.logVerbose(key + ": " +  systemProperties.get(key));
        //}
        return systemProperties
    }

    private fun getSystemPropertyWithAndroidAPI(property: String): String? {
        return try {
            System.getProperty(property)
        } catch (e: Exception) {
            XLog.e("Failed to get system property '" + property + "':" + e.message)
            null
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentMilliSecondUTCTimeStamp(): String {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z", Locale.getDefault())
        // df.timeZone = TimeZone.getTimeZone("UTC")
        return df.format(Date())
    }
}