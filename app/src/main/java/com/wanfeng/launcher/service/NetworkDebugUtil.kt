package com.wanfeng.launcher.service

import android.util.Log

object NetworkDebugUtil {

    private const val TAG = "NetDebug"

    data class HttpResult(
        val url:        String,
        val exitCode:   Int,
        val httpStatus: Int,
        val headers:    String,
        val body:       String,
        val error:      String,
        val durationMs: Long,
    ) {
        fun contains(kw: String) = body.contains(kw)
        fun summary() = "HTTP $httpStatus | ${body.length}chars | ${durationMs}ms"
    }

    fun fetch(url: String, timeoutSec: Int = 15): HttpResult {
        val t0 = System.currentTimeMillis()
        Log.d(TAG, "=".repeat(60))
        Log.d(TAG, "REQUEST  url=$url")

        return try {
            // ── Step 1：仅获取响应头和状态码（-I HEAD 请求）─────────────────
            val headProc = ProcessBuilder(
                "curl", "-s", "-I", "-L", "-k",
                "--max-redirs", "5",
                "--connect-timeout", timeoutSec.toString(),
                "--max-time", timeoutSec.toString(),
                url,
            ).redirectErrorStream(true).start()
            val headRaw    = headProc.inputStream.bufferedReader(Charsets.UTF_8).readText()
            headProc.waitFor()
            // 取最后一个 HTTP 状态行（跟了多次跳转可能有多段 header）
            val httpStatus = Regex("""HTTP/\S+\s+(\d+)""")
                .findAll(headRaw).lastOrNull()
                ?.groupValues?.get(1)?.toIntOrNull() ?: -1
            val finalUrl = Regex("""(?i)location:\s*(.+)""")
                .findAll(headRaw).lastOrNull()
                ?.groupValues?.get(1)?.trim() ?: url

            // ── Step 2：GET 请求，只拿 body（不混 header）────────────────────
            val bodyProc = ProcessBuilder(
                "curl", "-s", "-L", "-k",
                "--max-redirs", "5",
                "--connect-timeout", timeoutSec.toString(),
                "--max-time", timeoutSec.toString(),
                url,
            ).redirectErrorStream(false).start()
            val body   = bodyProc.inputStream.bufferedReader(Charsets.UTF_8).readText()
            val stderr = bodyProc.errorStream.bufferedReader(Charsets.UTF_8).readText()
            val code   = bodyProc.waitFor()
            val ms     = System.currentTimeMillis() - t0

            // ── 打印日志 ──────────────────────────────────────────────────────
            Log.d(TAG, "-".repeat(60))
            Log.d(TAG, "RESULT   exitCode=$code | HTTP $httpStatus | ${ms}ms")
            Log.d(TAG, "REDIRECT finalUrl=$finalUrl")
            Log.d(TAG, "----- HEADERS -----")
            headRaw.lines().take(20).forEach { Log.d(TAG, "  $it") }
            Log.d(TAG, "----- BODY (${body.length} chars) -----")
            body.chunked(400).forEachIndexed { i, chunk ->
                Log.d(TAG, "  [body#$i] $chunk")
            }
            if (stderr.isNotEmpty()) {
                Log.w(TAG, "----- STDERR -----")
                stderr.lines().forEach { Log.w(TAG, "  $it") }
            }
            Log.d(TAG, "=".repeat(60))

            HttpResult(url, code, httpStatus, headRaw, body, stderr, ms)
        } catch (e: Exception) {
            val ms = System.currentTimeMillis() - t0
            Log.e(TAG, "EXCEPTION after ${ms}ms: ${e.message}")
            HttpResult(url, -1, -1, "", "", e.message ?: "", ms)
        }
    }
}
