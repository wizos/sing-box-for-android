package io.mo.viaport.fmt

import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.ConsolePrinter
import com.elvishew.xlog.printer.Printer
import io.mo.viaport.ktx.readText
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import kotlin.time.measureTime

class FormatsKtTest {
    @Before
    fun initLog() {
        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .enableStackTrace(1)
            // .stackTraceFormatter(SingleStackTraceFormatter())
            .build()
        val consolePrinter: Printer = ConsolePrinter()
        XLog.init(config, consolePrinter)
    }

    @Test
    fun parseProxies(): Unit = runBlocking{
        FileInputStream(File("../test/nodes.txt")).readText()?.let { text ->
            val doc = parseProxies(text)
            // doc.outputSettings().prettyPrint(false)
            // doc.formatAttribute()
            val time1 = measureTime {
                XLog.d("解析后文本：" + doc )
            }
            XLog.d("耗时：" + time1  )

            // Extractor(doc).getContentElement()?.apply {
            //     XLog.d("路径为：" + this.prettyCssSelector())
            //     XLog.d("抽取结果：" + this.html())
            // }
        }
    }
}