package com.wanfeng.launcher.service

import android.util.Log

/**
 * 封装所有需要 root 权限的 shell 操作。
 * 所有函数均为阻塞调用，须在协程/子线程中使用。
 */
object RootUtil {

    private const val TAG = "RootUtil"

    // ──────────────────────────────────────────────────────────────────────────
    // 基础执行
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 以 root 执行单条命令，返回 (exitCode, stdout, stderr)。
     */
    fun exec(cmd: String): Triple<Int, String, String> {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            val code = process.waitFor()
            Log.d(TAG, "exec [$cmd] → $code")
            Triple(code, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            Log.e(TAG, "exec failed: ${e.message}")
            Triple(-1, "", e.message ?: "")
        }
    }

    /**
     * 执行脚本文件（后台，不等待返回值）。
     */
    fun execScriptAsync(scriptPath: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", "sh $scriptPath &"))
            Log.d(TAG, "execScriptAsync: $scriptPath")
        } catch (e: Exception) {
            Log.e(TAG, "execScriptAsync failed: ${e.message}")
        }
    }

    /**
     * 执行脚本文件（阻塞，等待完成）。
     * @return exitCode
     */
    fun execScript(scriptPath: String): Int {
        val (code, _, _) = exec("sh $scriptPath")
        return code
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 进程管理
    // ──────────────────────────────────────────────────────────────────────────

    /** 强制停止 Android 应用进程 */
    fun forceStop(packageName: String) {
        exec("am force-stop $packageName")
        // 双保险：kill 残留进程
        exec("pkill -f $packageName 2>/dev/null || true")
        Log.d(TAG, "forceStop: $packageName")
    }

    /** 检查进程是否存活，true = 存活 */
    fun isProcessAlive(packageName: String): Boolean {
        val (_, stdout, _) = exec("pgrep -f $packageName")
        return stdout.isNotEmpty()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 文件操作
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 解压 zip 文件到目标目录（root 上下文）。
     * 使用系统 unzip 命令确保写入 /data/local/tmp 有权限。
     */
    fun unzipToDir(zipPath: String, destDir: String): Boolean {
        exec("mkdir -p $destDir")
        val (code, _, err) = exec("unzip -o $zipPath -d $destDir")
        if (code != 0) Log.e(TAG, "unzip failed: $err")
        return code == 0
    }

    // ──────────────────────────────────────────────────────────────────────────
    // VPN 服务拉起
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 通过 am startservice 拉起 Service。
     *
     * serviceClass 既支持相对类名（如 .service.SimpleVpnService），
     * 也支持完整组件名（如 com.example.app/.service.SimpleVpnService）。
     */
    fun startVpnService(packageName: String, serviceClass: String) {
        val component = if ('/' in serviceClass) serviceClass else "$packageName/$serviceClass"
        exec("am startservice -n $component")
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 网络检测（通过 curl，root 执行）
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 访问 URL，读取前 20 行内容，判断是否包含 keyword。
     * 返回 null 表示请求失败，返回 String 为原始内容。
     */
    fun fetchUrl(url: String, timeoutSec: Int = 8): String? {
        return try {
            val (code, stdout, _) =
                exec("curl -s --connect-timeout $timeoutSec --max-time $timeoutSec '$url' 2>/dev/null | head -20")
            if (code == 0 && stdout.isNotEmpty()) stdout else null
        } catch (e: Exception) {
            Log.e(TAG, "fetchUrl failed: ${e.message}")
            null
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 终止自身进程
    // ──────────────────────────────────────────────────────────────────────────

    /** 以 root 权限强制 kill 自身进程 PID */
    fun killSelf() {
        val pid = android.os.Process.myPid()
        exec("kill -9 $pid")
        // 备用：java 层直接退出
        android.os.Process.killProcess(pid)
    }
}
