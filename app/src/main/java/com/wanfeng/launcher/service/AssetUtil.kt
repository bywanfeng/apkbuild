package com.wanfeng.launcher.service

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object AssetUtil {

    private const val TAG = "AssetUtil"
    // 脚本目标目录：su 对此目录有完整读写执行权限
    private const val SCRIPT_DIR = "/data/adb/tmp"

    fun readText(context: Context, assetPath: String): String? {
        return try {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "readText($assetPath) failed: ${e.message}")
            null
        }
    }

    fun randomLine(context: Context, assetPath: String, fallback: String = ""): String {
        val text = readText(context, assetPath) ?: return fallback
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return fallback
        return lines.random()
    }

    fun copyAssetToCache(context: Context, assetPath: String): File {
        val dest = File(context.cacheDir, File(assetPath).name)
        if (dest.exists()) dest.delete()
        context.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        Log.d(TAG, "copyAssetToCache: $assetPath -> ${dest.absolutePath}")
        return dest
    }

    /**
     * 提取 shell 脚本到 /data/adb/tmp/<name>，chmod 777。
     * 流程：assets -> cacheDir (普通IO) -> /data/adb/tmp (root cp)
     * 返回最终可执行路径。
     */
    fun extractScript(context: Context, scriptName: String): String {
        val destPath = "$SCRIPT_DIR/$scriptName"

        // Step 1: 写到 cacheDir（不需要 root）
        val cacheFile = File(context.cacheDir, scriptName)
        if (cacheFile.exists()) cacheFile.delete()
        context.assets.open("shell/$scriptName").use { input ->
            FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
        }
        Log.d(TAG, "extractScript cached: ${cacheFile.absolutePath}")

        // Step 2: root 复制到 /data/adb/tmp + chmod 777
        try {
            exec("mkdir -p $SCRIPT_DIR")
            val (code, out, err) = execFull("cp ${cacheFile.absolutePath} $destPath && chmod 777 $destPath")
            Log.d(TAG, "extractScript cp exitCode=$code out='$out' err='$err'")
            if (code != 0) {
                Log.e(TAG, "extractScript cp failed, falling back to cacheDir")
                return cacheFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e(TAG, "extractScript exception: ${e.message}")
            return cacheFile.absolutePath
        }

        Log.d(TAG, "extractScript ready: $destPath")
        return destPath
    }

    private fun exec(cmd: String) {
        Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)).waitFor()
    }

    private fun execFull(cmd: String): Triple<Int, String, String> {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        val out = p.inputStream.bufferedReader().readText().trim()
        val err = p.errorStream.bufferedReader().readText().trim()
        return Triple(p.waitFor(), out, err)
    }
}
