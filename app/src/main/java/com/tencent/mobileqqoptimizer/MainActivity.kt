package com.tencent.mobileqqoptimizer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var logTv: TextView

    private lateinit var btn: Button

    private lateinit var docTv: TextView

    private lateinit var mainTimeTick: Handler

    // 为了显示进度条，是个假的，只会显示 1...99%，大概耗时就在 90 s 左右。所以勉强够用。
    private var processInt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        mainTimeTick = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (processInt < 99) {
                    processInt++
                }
                appendLogAndScroll("优化中, 进度 ${processInt}%")

                sendEmptyMessageDelayed(0, 1000L)
            }
        }

        logTv = findViewById(R.id.trigger_optimizer_log_tv)
        logTv.movementMethod = ScrollingMovementMethod.getInstance()

        docTv = findViewById(R.id.optimizer_doc_tv)
        val docStr = "<br>QQ : 735763610" +
                "<br>邮箱: eeeli@tencent.com" +
                "<br><a href = 'https://docs.qq.com/doc/DRWRjU3pwQ3NBem9m'>原理文档 by eeeli</a>"
        docTv.text = Html.fromHtml(docStr)
        docTv.movementMethod = LinkMovementMethod.getInstance()

        btn = findViewById(R.id.trigger_optimizer_btn)
        btn.setOnClickListener {
            Log.i(TAG, "doDex2oat start.")
            Thread {
                try {
                    val cmd = "cmd package compile -m speed-profile -f com.tencent.mobileqq"
                    appendLogAndScroll("正在执行：$cmd，请稍等。")
                    beforeOptimizerExecute()

                    val p = Runtime.getRuntime().exec(cmd)
                    showNormalLog(p.inputStream)
                    showErrorLog(p.errorStream)
                    p.waitFor()

                    whenOptimizerExecuted()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
        testCompileFunction()
    }

    private fun testCompileFunction() {
        Thread {
            try {
                val cmd = "cmd package compile -m speed-profile -f com.tencent.mobileqqoptimizer"
                appendLogAndScroll("优化可行性验证中，请稍等。")

                val p = Runtime.getRuntime().exec(cmd)
                var isSuccess = false
                BufferedReader(InputStreamReader(p.inputStream)).use { bufferReader ->
                    var line = ""
                    while (bufferReader.readLine()?.also { line = it } != null) {
                        runOnUiThread {
                            Log.i(TAG, line)
                        }
                        if (line == "Success") {
                            isSuccess = true
                        }
                    }
                }
                p.waitFor()

                if (isSuccess) {
                    appendLogAndScroll("机型可行性验证成功。点击【优化手Q】，即刻开始优化手Q性能。")
                    appendLogAndScroll("通过系统设置页退出手Q后触发优化可增加成功概率。")
                } else {
                    runOnUiThread {
                        btn.isEnabled = false
                        appendLogAndScroll("优化可行性验证失败。暂不支持该机型优化。将于 10 秒后退出。")
                    }
                    mainTimeTick.sendMessageDelayed(
                        Message.obtain(mainTimeTick) {
                            exitProcess(-1)
                        }, 10000L
                    )
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun beforeOptimizerExecute() {

        processInt = 0
        runOnUiThread {
            btn.isEnabled = false
        }
        showTimeTick()
    }

    private fun whenOptimizerExecuted() {
        runOnUiThread {
            btn.isEnabled = true
        }
        stopTimeTick()
    }

    private fun showNormalLog(inputStream: InputStream) {
        Thread {
            BufferedReader(InputStreamReader(inputStream)).use { bufferReader ->
                var isSuccess = false
                var line = ""
                while (bufferReader.readLine()?.also { line = it } != null) {
                    runOnUiThread {
                        appendLogAndScroll("[NORMAL] $line")
                        Log.i(TAG, line)
                    }
                    if (line == "Success") {
                        isSuccess = true
                    }
                }
                if (isSuccess) {
                    appendLogAndScroll("优化完成, 进度 100%.")
                } else {
                    appendLogAndScroll("优化失败. 优化过程中需要占用较多运存，请尝试退出其余后台任务后重试。")
                }
            }
        }.start()
    }

    private fun showErrorLog(inputStream: InputStream) {
        Thread {
            BufferedReader(InputStreamReader(inputStream)).use { bufferReader ->
                var isError = false
                var line = ""
                while (bufferReader.readLine()?.also { line = it } != null) {
                    isError = true
                    runOnUiThread {
                        appendLogAndScroll("[ERROR] $line")
                        Log.i(TAG, line)
                    }
                }
                if (isError) {
                    appendLogAndScroll("优化失败.")
                }
            }
        }.start()
    }

    private fun appendLogAndScroll(log: String) {
        logTv.append("\n${getTime()} $log")
        val offset = logTv.lineCount * logTv.lineHeight
        val factOffset = logTv.height - logTv.lineHeight
        if (offset > factOffset) {
            logTv.scrollBy(0, logTv.lineHeight)
        }
    }

    private fun showTimeTick() {
        Log.i(TAG, "showTimeTick.")
        mainTimeTick.sendEmptyMessage(0)
    }

    private fun stopTimeTick() {
        Log.i(TAG, "stopTimeTick.")
        mainTimeTick.removeCallbacksAndMessages(null)
    }

    private fun getTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
        return formatter.format(Date())
    }
}