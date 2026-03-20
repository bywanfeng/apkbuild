package com.wanfeng.launcher.service

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * 网络诊断工具：完整打印 HTTP 请求的状态码、响应头、响应体。
 * 在 ViewModel / 任意协程里调用，结果通过 logcat 和返回值双路输出。
 */
object NetworkDebugUtil {

    private const val TAG = "NetDebug"

    data class HttpResult(
        val url:        String,
        val exitCode:   Int,       // curl 进程退出码
        val httpStatus: Int,       // HTTP 状态码（-1 表示获取失败）
        val headers:    String,    // 响应头原文
        val body:       String,    // 响应体（完整）
        val error:      String,    // stderr / 异常信息
        val durationMs: Long,
    ) {
        fun contains(kw: String) = body.contains(kw)
        fun summary() = "HTTP $httpStatus | ${body.length}chars | ${durationMs}ms"
    }

    /**
     * 完整 HTTP 请求：
     *  - 跟随跳转（最多 5 次）
     *  - 忽略证书
     *  - 返回状态码 + 响应头 + 完整 body
     *  - 全程打印到 logcat TAG=NetDebug
     */
    fun fetch(url: String, timeoutSec: Int = 15): HttpResult {
        val t0 = System.currentTimeMillis()
        Log.d(TAG, "=".repeat(60))
        Log.d(TAG, "REQUEST  url=$url")
        Log.d(TAG, "OPTIONS  timeout=${timeoutSec}s  max-redirs=5  insecure=true")

        return try {
            // 用 curl -v 获取详细信息，-D- 把响应头输出到 stdout
            val proc = ProcessBuilder(
                "curl",
                "-s",           // 静默
                "-L",           // 跟随跳转
                "-k",           // 忽略证书
                "--max-redirs", "5",
                "--connect-timeout", timeoutSec.toString(),
                "--max-time",       timeoutSec.toString(),
                "-w", "\nHTTP_STATUS_CODE:%{http_code}\nREDIRECT_COUNT:%{num_redirects}\nFINAL_URL:%{url_effective}\n",
                "-D", "-",      // 把响应头输出到 stdout
                url,
            )
                .redirectErrorStream(false)
                .start()

            val stdout = proc.inputStream.bufferedReader(Charsets.UTF_8).readText()
            val stderr = proc.errorStream.bufferedReader(Charsets.UTF_8).readText()
            val code   = proc.waitFor()
            val ms     = System.currentTimeMillis() - t0

            // 解析 curl write-out 附加信息
            val httpStatus   = Regex("HTTP_STATUS_CODE:(\\d+)").find(stdout)?.groupValues?.get(1)?.toIntOrNull() ?: -1
            val redirectCount = Regex("REDIRECT_COUNT:(\\d+)").find(stdout)?.groupValues?.get(1) ?: "?"
            val finalUrl     = Regex("FINAL_URL:(.+)").find(stdout)?.groupValues?.get(1)?.trim() ?: url

            // 分离响应头（第一个空行之前）和 body
            val headerEnd = stdout.indexOf("\r\n\r\n").takeIf { it >= 0 }
                ?: stdout.indexOf("\n\n").takeIf { it >= 0 }
                ?: 0
            val headers = if (headerEnd > 0) stdout.substring(0, headerEnd) else ""
            // body 去掉末尾的 write-out 附加信息
            val bodyRaw = if (headerEnd > 0) stdout.substring(headerEnd).trimStart() else stdout
            val body = bodyRaw
                .replace(Regex("\nHTTP_STATUS_CODE:\\d+.*$", RegexOption.DOT_MATCHES_ALL), "")
                .trim()

            // ── 打印完整日志 ──────────────────────────────────────────────────
            Log.d(TAG, "-".repeat(60))
            Log.d(TAG, "RESULT   exitCode=$code | HTTP $httpStatus | ${ms}ms")
            Log.d(TAG, "REDIRECT count=$redirectCount | finalUrl=$finalUrl")
            Log.d(TAG, "----- HEADERS -----")
            headers.lines().forEach { Log.d(TAG, "  $it") }
            Log.d(TAG, "----- BODY (${body.length} chars) -----")
            // 每 400 字一段打印，防止 logcat 截断
            body.chunked(400).forEachIndexed { i, chunk ->
                Log.d(TAG, "  [body#$i] $chunk")
            }
            if (stderr.isNotEmpty()) {
                Log.w(TAG, "----- STDERR -----")
                stderr.lines().forEach { Log.w(TAG, "  $it") }
            }
            Log.d(TAG, "=".repeat(60))

            HttpResult(
                url        = url,
                exitCode   = code,
                httpStatus = httpStatus,
                headers    = headers,
                body       = body,
                error      = stderr,
                durationMs = ms,
            )
        } catch (e: Exception) {
            val ms = System.currentTimeMillis() - t0
            Log.e(TAG, "EXCEPTION after ${ms}ms: ${e.message}")
            HttpResult(url, -1, -1, "", "", e.message ?: "", ms)
        }
    }
}
