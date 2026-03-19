package com.wanfeng.launcher.service

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object AssetUtil {

    private const val TAG = "AssetUtil"

    /** 读取 assets 中的文本文件，返回完整字符串；失败返回 null */
    fun readText(context: Context, assetPath: String): String? {
        return try {
            context.assets.open(assetPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            Log.e(TAG, "readText($assetPath) failed: ${e.message}")
            null
        }
    }

    /**
     * 从 assets 文本文件中随机读取一行非空内容。
     * @return 随机行文本，失败返回 fallback 值
     */
    fun randomLine(context: Context, assetPath: String, fallback: String = ""): String {
        val text = readText(context, assetPath) ?: return fallback
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return fallback
        return lines.random()
    }

    /**
     * 将 assets 中的文件复制到设备文件系统指定路径。
     * 用于将 bypass.zip 等大文件复制到 /data/local/tmp 前的暂存。
     * @return 复制后的文件路径
     */
    fun copyAssetToCache(context: Context, assetPath: String): File {
        val dest = File(context.cacheDir, File(assetPath).name)
        if (dest.exists()) dest.delete()
        context.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        Log.d(TAG, "copyAssetToCache: $assetPath → ${dest.absolutePath}")
        return dest
    }

    /**
     * 将 assets/shell 目录下的脚本文件复制到 cacheDir，并赋予可执行权限。
     * @return 可执行文件的绝对路径
     */
    fun extractScript(context: Context, scriptName: String): String {
        val dest = File(context.cacheDir, scriptName)
        if (dest.exists()) dest.delete()
        context.assets.open("shell/$scriptName").use { input ->
            FileOutputStream(dest).use { output ->
                input.copyTo(output)
            }
        }
        dest.setExecutable(true, false)
        Log.d(TAG, "extractScript: $scriptName → ${dest.absolutePath}")
        return dest.absolutePath
    }
}
