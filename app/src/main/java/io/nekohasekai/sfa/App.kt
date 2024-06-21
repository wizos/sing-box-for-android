package io.nekohasekai.sfa

import android.app.Application
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.PowerManager
import androidx.core.content.getSystemService
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.Printer
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator
import go.Seq
import io.nekohasekai.sfa.bg.AppChangeReceiver
import io.nekohasekai.sfa.bg.UpdateProfileWork
import io.mo.viaport.helper.CrashHandler
import io.mo.viaport.helper.ioScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import io.nekohasekai.sfa.App as BoxApplication

class App : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        Seq.setContext(this)

        @Suppress("OPT_IN_USAGE")
        GlobalScope.launch(Dispatchers.IO) {
            UpdateProfileWork.reconfigureUpdater()
        }

        ioScope.launch {
            // 监听子线程的报错
            Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
        }

        registerReceiver(AppChangeReceiver(), IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addDataScheme("package")
        })

        val config = LogConfiguration.Builder()
            .logLevel(if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.INFO)
            .tag(BuildConfig.applicationName)
            .enableStackTrace(1)
            // .stackTraceFormatter(SingleStackTraceFormatter())
            .build()
        val androidPrinter: Printer = AndroidPrinter()
        val filePrinter: Printer =
            FilePrinter.Builder(externalCacheDir.toString() + "/log/") // 指定保存日志文件的路径
                .flattener(ClassicFlattener())
                .fileNameGenerator(DateFileNameGenerator())           // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                // .backupStrategy(new NeverBackupStrategy())         // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                // .shouldBackup()
                .cleanStrategy(FileLastModifiedCleanStrategy(3 * 24 * 3600 * 1000)) // 指定日志文件清除策略，默认为 NeverCleanStrategy()
                .build()

        // 初始化 XLog
        XLog.init(config, androidPrinter, filePrinter)
    }

    companion object {
        @JvmStatic
        lateinit var instance: BoxApplication
        val notification by lazy { instance.getSystemService<NotificationManager>()!! }
        val connectivity by lazy { instance.getSystemService<ConnectivityManager>()!! }
        val packageManager by lazy { instance.packageManager }
        val powerManager by lazy { instance.getSystemService<PowerManager>()!! }
        val notificationManager by lazy { instance.getSystemService<NotificationManager>()!! }
        val wifiManager by lazy { instance.getSystemService<WifiManager>()!! }
        val clipboard by lazy { instance.getSystemService<ClipboardManager>()!! }
    }

}