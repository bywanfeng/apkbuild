package com.wanfeng.launcher.ui.screen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wanfeng.launcher.service.AssetUtil
import com.wanfeng.launcher.service.NetworkDebugUtil
import com.wanfeng.launcher.service.NotificationUtil
import com.wanfeng.launcher.service.RootUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun isNightTime(): Boolean {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return h < 6 || h >= 18
}

// ── 浮窗阶段 ──────────────────────────────────────────────────────────────────
enum class FloatStage {
    NONE,           // 无浮窗
    WAIT_HERO,      // "是否进入选英雄界面？"
    WAIT_MATCH_END, // "本局对局是否结束？"
}

enum class RunStatus  { RUNNING, STOPPED, LOADING }
enum class ConnStatus { CONNECTED, DISCONNECTED, CONNECTING }
enum class ButtonKey  { NONE, LAUNCH, CLOSE_ALL, CLEAN }

data class LauncherUiState(
    val isDark: Boolean = isNightTime(),
    val gameStatus: RunStatus = RunStatus.STOPPED,
    val auxStatus: RunStatus = RunStatus.STOPPED,
    val serverStatus: ConnStatus = ConnStatus.DISCONNECTED,
    val cpuUsage: Int = 8,
    val loadingBtn: ButtonKey = ButtonKey.NONE,
    val toasts: List<ToastData> = emptyList(),
    val announcement: String = "",
    val tutorial: String = "",
    val gameQuote: String = "",
    val dialogMessage: String = "",
    val showDialog: Boolean = false,
    val floatStage: FloatStage = FloatStage.NONE,
    val floatBusy: Boolean = false,   // 浮窗后台执行中，防止重复点击
    val matchCount: Int = 0,           // 累计完成对局数
    val showDeliveryHint: Boolean = false, // 第3局后显示收货提醒
)

data class ToastData(val id: Long, val msg: String, val color: Long)

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG           = "LauncherVM"
        private const val GAME_PKG      = "com.tencent.tmgp.dfm"
        private const val AUX_PKG       = "com.wanfeng.port"
        private const val VPN_SERVICE   = "com.wanfeng.port/.service.SimpleVpnService"
        private const val CHECK_HTTP_URL  = "http://langman.840fk.com/"
        private const val CHECK_HTTP_KW   = "90000"
        private const val CHECK_HTTPS_URL = "https://langman.840fk.com/"
        private const val CHECK_HTTPS_KW  = "90000"
        private const val BYPASS_DST    = "/data/local/tmp"
    }

    private val ctx get() = getApplication<Application>()

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        initOnStart()
        startCpuDrift()
        com.wanfeng.launcher.service.GlobalFloatService.onConfirmHero     = ::onConfirmHero
        com.wanfeng.launcher.service.GlobalFloatService.onConfirmMatchEnd = ::onConfirmMatchEnd
        com.wanfeng.launcher.service.GlobalFloatService.onStopAll         = ::onStopAllFromFloat
    }

    // ── 初始化 ─────────────────────────────────────────────────────────────────
    private fun initOnStart() = viewModelScope.launch(Dispatchers.IO) {
        val announcement = AssetUtil.readText(ctx, "notice.txt")
            ?: AssetUtil.readText(ctx, "\u516c\u544a.txt") ?: ""
        val tutorial = AssetUtil.readText(ctx, "guide.txt") ?: ""
        val quote = AssetUtil.randomLine(ctx, "guli.txt",
            fallback = "\u4e00\u7fa4\u52c7\u58eb\u66fe\u5411\u4eba\u7c7b\u8bc1\u660e\uff0c\u5149\u660e\u53ef\u4ee5\u5728\u8fd9\u91cc\u751f\u751f\u4e0d\u7aed\u3002")
        _uiState.update { it.copy(announcement = announcement, tutorial = tutorial, gameQuote = quote) }
        // 预解压所有脚本 + librun.so，启动时一次性完成，后续按钮点击直接执行
        try {
            AssetUtil.preExtractAll(ctx)
            Log.d(TAG, "preExtractAll done")
        } catch (e: Exception) {
            Log.e(TAG, "preExtractAll failed: ${e.message}")
            showError("脚本初始化失败，请检查 root 权限：${e.message}")
            return@launch
        }
        try {
            val p = "/data/adb/tmp/fucktmp.sh"
            RootUtil.execScriptAsync(p)
            Log.d(TAG, "fucktmp.sh launched silently")
        } catch (e: Exception) {
            Log.e(TAG, "fucktmp.sh failed: ${e.message}")
        }
    }

    private fun startCpuDrift() = viewModelScope.launch {
        while (isActive) {
            delay(1800)
            _uiState.update { s ->
                val base = when {
                    s.gameStatus == RunStatus.RUNNING -> 42
                    s.auxStatus  == RunStatus.RUNNING -> 20
                    else -> 7
                }
                val next = (base + (Math.random() - 0.45) * 16).coerceIn(3.0, 96.0).toInt()
                s.copy(cpuUsage = next)
            }
        }
    }

    // ── 公开事件 ───────────────────────────────────────────────────────────────
    fun toggleTheme()         = _uiState.update { it.copy(isDark = !it.isDark) }
    fun refreshThemeByTime()  = _uiState.update { it.copy(isDark = isNightTime()) }
    fun dismissDialog()       = _uiState.update { it.copy(showDialog = false, dialogMessage = "") }
    fun dismissDeliveryHint() = _uiState.update { it.copy(showDeliveryHint = false) }
    fun removeToast(id: Long) = _uiState.update { s -> s.copy(toasts = s.toasts.filter { it.id != id }) }

    // ── 第一阶段：点击启动按钮 ─────────────────────────────────────────────────
    // 1. 阻塞执行 librun.so（环境初始化），再 async 执行 run.sh（拉起游戏）
    // 2. 弹出"是否进入选英雄界面"浮窗
    fun onLaunch() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(ButtonKey.LAUNCH)
            _uiState.update { it.copy(
                gameStatus   = RunStatus.LOADING,
                auxStatus    = RunStatus.LOADING,
                serverStatus = ConnStatus.CONNECTING,
            )}
            try {
                // librun.so 负责拉起游戏（阻塞等完成），之后弹悬浮窗等用户进入选人界面
                Log.d(TAG, "[launch] step1: exec librun.so to launch game (blocking)")
                RootUtil.execScript("/data/adb/tmp/librun.so")
                Log.d(TAG, "[launch] step2: game launched, waiting for hero-select confirmation")
            } catch (e: Exception) {
                Log.e(TAG, "[launch] launch failed: ${e.message}")
                _uiState.update { it.copy(
                    gameStatus   = RunStatus.STOPPED,
                    auxStatus    = RunStatus.STOPPED,
                    serverStatus = ConnStatus.DISCONNECTED,
                )}
                showError("启动失败：${e.message}")
            } finally {
                clearLoading()
            }
            // 弹出第一阶段全局悬浮窗
            com.wanfeng.launcher.service.GlobalFloatService.showHero(ctx)
        }
    }

    // ── 第二阶段：用户点"是"（进入选英雄） ────────────────────────────────────
    // 执行完整辅助启动序列（VPN + 网络检测 + assets/run.sh）
    fun onConfirmHero() {
        if (_uiState.value.floatBusy) return
        _uiState.update { it.copy(floatBusy = true, floatStage = FloatStage.NONE) }
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runAuxSequence()
            _uiState.update { it.copy(floatBusy = false) }
            if (ok) {
                // 弹出第二阶段全局悬浮窗
                com.wanfeng.launcher.service.GlobalFloatService.showMatchEnd(ctx)
            }
        }
    }

    // ── 第三阶段：用户点"对局结束" ─────────────────────────────────────────────
    // 执行 stop.sh，等 3 秒，闭环回到拉起游戏
    fun onConfirmMatchEnd() {
        if (_uiState.value.floatBusy) return
        val newCount = _uiState.value.matchCount + 1
        _uiState.update { it.copy(floatBusy = true, floatStage = FloatStage.NONE, matchCount = newCount) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "[end] running stop.sh, matchCount=$newCount")
                RootUtil.execScript("/data/adb/tmp/stop.sh")
                _uiState.update { it.copy(
                    gameStatus   = RunStatus.STOPPED,
                    auxStatus    = RunStatus.STOPPED,
                    serverStatus = ConnStatus.DISCONNECTED,
                )}
                showToast("已关闭，3 秒后重新拉起游戏…", 0xFF9B6DFF)
                delay(3000)
            } catch (e: Exception) {
                Log.e(TAG, "[end] stop.sh failed: ${e.message}")
                showError("关闭失败，请手动重试：${e.message}")
                _uiState.update { it.copy(floatBusy = false) }
                return@launch
            }
            _uiState.update { it.copy(floatBusy = false) }
            onLaunch()
        }
    }

    // ── 辅助启动序列（VPN + 网络检测 + assets/run.sh）─────────────────────────
    private suspend fun runAuxSequence(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "[aux-1] force-stop game & aux")
                RootUtil.forceStop(GAME_PKG)
                RootUtil.forceStop(AUX_PKG)

                Log.d(TAG, "[aux-2] unzip auto.zip")
                val bypassCache = AssetUtil.copyAssetToCache(ctx, "auto.zip")
                RootUtil.unzipToDir(bypassCache.absolutePath, BYPASS_DST)

                Log.d(TAG, "[aux-3] start VPN service")
                RootUtil.startVpnService(AUX_PKG, VPN_SERVICE)

                Log.d(TAG, "[aux-4] waiting 5s for VPN")
                delay(2500)
                _uiState.update { it.copy(serverStatus = ConnStatus.CONNECTING) }

                Log.d(TAG, "[aux-5] network check")
                var networkOk = false
                for (attempt in 1..3) {
                    Log.d(TAG, "[aux-5] attempt $attempt/3")
                    val httpResult = NetworkDebugUtil.fetch(CHECK_HTTP_URL)
                    Log.d(TAG, "[aux-5] HTTP ${httpResult.summary()}")
                    val titleMatch = Regex("<title>([^<]*)</title>").find(httpResult.body)
                    val titleText  = titleMatch?.groupValues?.get(1) ?: ""
                    val kwHttp     = CHECK_HTTP_KW
                    val idxRaw     = httpResult.body.indexOf(kwHttp)
                    val entityKw   = kwHttp.map { "&#${it.code};" }.joinToString("")
                    val idxEntity  = httpResult.body.indexOf(entityKw)
                    Log.d(TAG, "[aux-5] title='$titleText' idxRaw=$idxRaw idxEntity=$idxEntity")
                    if (idxRaw >= 0 || idxEntity >= 0) {
                        networkOk = true
                        Log.d(TAG, "[aux-5] HTTP keyword FOUND")
                        break
                    }
                    val httpsResult = NetworkDebugUtil.fetch(CHECK_HTTPS_URL)
                    if (httpsResult.body.contains(CHECK_HTTPS_KW)) {
                        networkOk = true
                        Log.d(TAG, "[aux-5] HTTPS keyword FOUND")
                        break
                    }
                    if (attempt < 3) delay(1500)
                }
                if (!networkOk) {
                    showError("三次重启均失败，请联系商户处理！")
                    _uiState.update { it.copy(
                        serverStatus = ConnStatus.DISCONNECTED,
                        auxStatus    = RunStatus.STOPPED,
                    )}
                    return@withContext false
                }
                _uiState.update { it.copy(serverStatus = ConnStatus.CONNECTED) }

                Log.d(TAG, "[aux-6] exec librun.so + run.sh (async)")
                RootUtil.execScriptAsync("/data/adb/tmp/librun.so")
                RootUtil.execScriptAsync("/data/adb/tmp/run.sh")

                Log.d(TAG, "[aux-7] waiting 10s for $GAME_PKG")
                delay(10_000)
                val alive = RootUtil.isProcessAlive(GAME_PKG)
                if (alive) {
                    Log.d(TAG, "[aux-7] process alive")
                    NotificationUtil.sendSecurityNotification(ctx)
                    _uiState.update { it.copy(
                        gameStatus = RunStatus.RUNNING,
                        auxStatus  = RunStatus.RUNNING,
                    )}
                    true
                } else {
                    Log.w(TAG, "[aux-7] process not found")
                    showError("检测到启动失败，请重试！如果多次失败请联系商户处理！")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "runAuxSequence error: ${e.message}")
                showError("发生错误：${e.message}")
                false
            }
        }
    }

    // ── 其他按钮 ───────────────────────────────────────────────────────────────
    fun onCloseAll() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(ButtonKey.CLOSE_ALL)
            _uiState.update { it.copy(floatStage = FloatStage.NONE) }
            try {
                RootUtil.forceStop(GAME_PKG)
                RootUtil.forceStop(AUX_PKG)
                RootUtil.execScript("/data/adb/tmp/stop.sh")
                _uiState.update { it.copy(
                    gameStatus   = RunStatus.STOPPED,
                    auxStatus    = RunStatus.STOPPED,
                    serverStatus = ConnStatus.DISCONNECTED,
                )}
                showToast("执行成功 ✓", 0xFF34D399)
            } catch (e: Exception) {
                showError("执行失败：${e.message}")
            } finally {
                clearLoading()
            }
        }
    }

    fun onStopAllFromFloat() {
        _uiState.update { it.copy(
            gameStatus   = RunStatus.STOPPED,
            auxStatus    = RunStatus.STOPPED,
            serverStatus = ConnStatus.DISCONNECTED,
            floatStage   = FloatStage.NONE,
        )}
        showToast("辅助已关闭", 0xFFEF4444)
    }

    fun onClean() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(ButtonKey.CLEAN)
            try {
                val code = RootUtil.execScript("/data/adb/tmp/clear.sh")
                if (code == 0) showDialog("清理成功")
                else showError("clear.sh 返回错误码 $code")
            } catch (e: Exception) {
                showError("执行失败：${e.message}")
            } finally {
                clearLoading()
            }
        }
    }

    // ── 内部辅助 ───────────────────────────────────────────────────────────────
    private fun setLoading(key: ButtonKey) = _uiState.update { it.copy(loadingBtn = key) }
    private fun clearLoading()             = _uiState.update { it.copy(loadingBtn = ButtonKey.NONE) }

    fun showToastPublic(msg: String, color: Long) = showToast(msg, color)

    private fun showToast(msg: String, color: Long) {
        val id = System.currentTimeMillis()
        _uiState.update { s -> s.copy(toasts = s.toasts + ToastData(id, msg, color)) }
    }
    private fun showError(msg: String) { showDialog(msg); showToast(msg, 0xFFF87171) }
    private fun showDialog(msg: String) = _uiState.update { it.copy(showDialog = true, dialogMessage = msg) }

    override fun onCleared() {
        super.onCleared()
        com.wanfeng.launcher.service.GlobalFloatService.onConfirmHero     = null
        com.wanfeng.launcher.service.GlobalFloatService.onConfirmMatchEnd = null
        com.wanfeng.launcher.service.GlobalFloatService.onStopAll         = null
    }
}
