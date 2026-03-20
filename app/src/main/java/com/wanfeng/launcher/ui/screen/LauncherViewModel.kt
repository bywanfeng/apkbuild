package com.wanfeng.launcher.ui.screen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wanfeng.launcher.service.AssetUtil
import com.wanfeng.launcher.service.NotificationUtil
import com.wanfeng.launcher.service.RootUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 根据时间判断是否深色：19:00-次日07:00 为深色
private fun isNightTime(): Boolean {
    val h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return h >= 19 || h < 7
}

// ── 枚举 ──────────────────────────────────────────────────────────────────────
enum class RunStatus  { RUNNING, STOPPED, LOADING }
enum class ConnStatus { CONNECTED, DISCONNECTED, CONNECTING }
enum class ButtonKey  { NONE, LAUNCH, RESTART_AUX, CLOSE_ALL, CLEAN }

// ── UI 状态 ───────────────────────────────────────────────────────────────────
data class LauncherUiState(
    val isDark: Boolean = isNightTime(),   // 由时间自动初始化
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
)

data class ToastData(val id: Long, val msg: String, val color: Long)

// ── ViewModel ─────────────────────────────────────────────────────────────────
class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LauncherVM"
        private const val GAME_PKG    = "com.tencent.tmgp.dfm"
        private const val AUX_PKG     = "com.wanfeng.port"
        private const val VPN_SERVICE = "com.wanfeng.port/.service.SimpleVpnService"
        private const val CHECK_URL   = "http://183.2.172.46/"
        private const val CHECK_KW    = "花海"
        private const val BYPASS_DST  = "/data/local/tmp"
    }

    private val ctx get() = getApplication<Application>()

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        initOnStart()
        startCpuDrift()
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 初始化
    // ──────────────────────────────────────────────────────────────────────────

    private fun initOnStart() = viewModelScope.launch(Dispatchers.IO) {
        // 1. 读取公告与教程（本地 assets，不联网）
        val announcement =
            AssetUtil.readText(ctx, "notice.txt")
                ?: AssetUtil.readText(ctx, "公告.txt")
                ?: AssetUtil.readText(ctx, "#U516c#U544a.txt")
                ?: ""

        val tutorial =
            AssetUtil.readText(ctx, "guide.txt")
                ?: AssetUtil.readText(ctx, "教程.txt")
                ?: ""

        // 2. 从本地寄语文件中随机读取一行展示在首页
        val quote = AssetUtil.randomLine(
            ctx,
            "guli.txt",
            fallback = "欢迎使用晚风工作室服务面板，愿今天一切顺利。"
        )

        _uiState.update { it.copy(announcement = announcement, tutorial = tutorial, gameQuote = quote) }

        // 3. 静默执行 fucktmp.sh（忽略结果）
        try {
            val scriptPath = AssetUtil.extractScript(ctx, "fucktmp.sh")
            RootUtil.execScriptAsync(scriptPath)
            Log.d(TAG, "fucktmp.sh launched silently")
        } catch (e: Exception) {
            Log.e(TAG, "fucktmp.sh failed: ${e.message}")
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CPU 模拟抖动（与 App.tsx 行为一致）
    // ──────────────────────────────────────────────────────────────────────────

    private fun startCpuDrift() = viewModelScope.launch {
        while (true) {
            delay(1800)
            _uiState.update { s ->
                val base = when {
                    s.gameStatus == RunStatus.RUNNING -> 42
                    s.auxStatus == RunStatus.RUNNING  -> 20
                    else -> 7
                }
                val next = (base + (Math.random() - 0.45) * 16)
                    .coerceIn(3.0, 96.0).toInt()
                s.copy(cpuUsage = next)
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 公开 UI 事件
    // ──────────────────────────────────────────────────────────────────────────

    fun toggleTheme() = _uiState.update { it.copy(isDark = !it.isDark) }

    /** 每次回到前台时重新按时间刷新（若用户未手动切换过，则跟随时间）*/
    fun refreshThemeByTime() = _uiState.update { it.copy(isDark = isNightTime()) }

    fun dismissDialog() = _uiState.update { it.copy(showDialog = false, dialogMessage = "") }

    fun removeToast(id: Long) = _uiState.update { s ->
        s.copy(toasts = s.toasts.filter { it.id != id })
    }

    /** 主启动按钮：启动辅助 + 游戏 */
    fun onLaunch() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        launchSequence(scriptName = "run.sh", buttonKey = ButtonKey.LAUNCH)
    }

    /** 重启辅助程序（与 onLaunch 流程相同，但执行 reboot.sh） */
    fun onRestartAux() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        launchSequence(scriptName = "reboot.sh", buttonKey = ButtonKey.RESTART_AUX)
    }

    /** 完全关闭辅助和游戏 */
    fun onCloseAll() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(ButtonKey.CLOSE_ALL)
            try {
                // Step 1: 关闭双进程
                RootUtil.forceStop(GAME_PKG)
                RootUtil.forceStop(AUX_PKG)
                // Step 2: 执行 stop.sh
                val scriptPath = AssetUtil.extractScript(ctx, "stop.sh")
                RootUtil.execScript(scriptPath)

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            gameStatus = RunStatus.STOPPED,
                            auxStatus = RunStatus.STOPPED,
                            serverStatus = ConnStatus.DISCONNECTED,
                        )
                    }
                    showToast("执行成功 ✓", 0xFF34D399)
                }
            } catch (e: Exception) {
                showError("执行失败：${e.message}")
            } finally {
                clearLoading()
            }
        }
    }

    /** 清理防盗号 */
    fun onClean() {
        if (_uiState.value.loadingBtn != ButtonKey.NONE) return
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(ButtonKey.CLEAN)
            try {
                val scriptPath = AssetUtil.extractScript(ctx, "clear.sh")
                val code = RootUtil.execScript(scriptPath)
                if (code == 0) {
                    showDialog("清理成功")
                } else {
                    showError("clear.sh 返回错误码 $code")
                }
            } catch (e: Exception) {
                showError("执行失败：${e.message}")
            } finally {
                clearLoading()
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 核心：启动序列（run.sh 或 reboot.sh）
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * 启动流程（共7步）：
     * 1. 关闭 GAME_PKG + AUX_PKG
     * 2. 解压 bypass.zip → /data/local/tmp
     * 3. 启动 VPN 服务
     * 4. 等待 1.5s
     * 5. 网络检测 http://183.2.172.46/ 关键词"神念"，最多重试3次
     * 6. 执行 scriptName（run.sh / reboot.sh）
     * 7. 等待10s，检测 GAME_PKG 进程是否存活
     */
    private fun launchSequence(scriptName: String, buttonKey: ButtonKey) {
        viewModelScope.launch(Dispatchers.IO) {
            setLoading(buttonKey)
            _uiState.update {
                it.copy(
                    auxStatus = RunStatus.LOADING,
                    gameStatus = RunStatus.LOADING,
                    serverStatus = ConnStatus.CONNECTING,
                )
            }

            try {
                // ── Step 1: 关闭进程 ──────────────────────────────────────────
                Log.d(TAG, "[1] force-stop $GAME_PKG & $AUX_PKG")
                RootUtil.forceStop(GAME_PKG)
                RootUtil.forceStop(AUX_PKG)

                // ── Step 2: 解压 bypass.zip ───────────────────────────────────
                Log.d(TAG, "[2] unzip bypass.zip → $BYPASS_DST")
                val bypassCache = AssetUtil.copyAssetToCache(ctx, "bypass.zip")
                val unzipOk = RootUtil.unzipToDir(bypassCache.absolutePath, BYPASS_DST)
                if (!unzipOk) Log.w(TAG, "bypass.zip 解压警告：部分文件可能失败，继续执行")

                // ── Step 3: 拉起 VPN 服务 ─────────────────────────────────────
                Log.d(TAG, "[3] start VPN service")
                RootUtil.startVpnService(AUX_PKG, VPN_SERVICE)

                // ── Step 4: 等待 VPN 建连（5s）────────────────────────────
                Log.d(TAG, "[4] waiting 5s for VPN to establish...")
                delay(5000)

                // ── Step 5: 网络检测（最多重试3次，每次间隔2s）─────────────────
                Log.d(TAG, "[5] network check: $CHECK_URL keyword='$CHECK_KW'")
                var networkOk = false
                for (attempt in 1..3) {
                    Log.d(TAG, "[5] attempt $attempt/3 url=$CHECK_URL kw=$CHECK_KW")
                    val body = RootUtil.fetchUrl(CHECK_URL)
                    Log.d(TAG, "[5] body=${if (body == null) "NULL" else "${body.length}chars: ${body.take(200)}"}")
                    if (body != null && body.contains(CHECK_KW)) {
                        networkOk = true
                        Log.d(TAG, "[5] keyword FOUND, continuing")
                        break
                    } else {
                        Log.w(TAG, "[5] keyword NOT found in body, attempt=$attempt")
                    }
                    if (attempt < 3) delay(3000)
                }
                if (!networkOk) {
                    showError("三次重启均失败，请联系商户处理！")
                    _uiState.update {
                        it.copy(
                            auxStatus = RunStatus.STOPPED,
                            gameStatus = RunStatus.STOPPED,
                            serverStatus = ConnStatus.DISCONNECTED,
                        )
                    }
                    clearLoading()
                    return@launch
                }

                // ── Step 6: 执行脚本 ──────────────────────────────────────────
                Log.d(TAG, "[6] exec $scriptName")
                val scriptPath = AssetUtil.extractScript(ctx, scriptName)
                RootUtil.execScriptAsync(scriptPath)

                // ── Step 7: 等待10s，检测进程 ─────────────────────────────────
                Log.d(TAG, "[7] waiting 10s for $GAME_PKG")
                delay(10_000)
                val alive = RootUtil.isProcessAlive(GAME_PKG)
                if (alive) {
                    // 进程存活：发送安全通知，然后终止自身
                    Log.d(TAG, "[7] process alive → notify + kill self")
                    NotificationUtil.sendSecurityNotification(ctx)
                    delay(300) // 留时间让通知发送
                    RootUtil.killSelf()
                } else {
                    // 进程不存在：启动失败
                    Log.w(TAG, "[7] process not found → launch failed")
                    showError("检测到启动失败，请重试！如果多次失败请联系商户处理！")
                    _uiState.update {
                        it.copy(
                            auxStatus = RunStatus.STOPPED,
                            gameStatus = RunStatus.STOPPED,
                            serverStatus = ConnStatus.DISCONNECTED,
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "launchSequence error: ${e.message}")
                showError("发生错误：${e.message}")
                _uiState.update {
                    it.copy(
                        auxStatus = RunStatus.STOPPED,
                        gameStatus = RunStatus.STOPPED,
                        serverStatus = ConnStatus.DISCONNECTED,
                    )
                }
            } finally {
                clearLoading()
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 内部辅助
    // ──────────────────────────────────────────────────────────────────────────

    private fun setLoading(key: ButtonKey) =
        _uiState.update { it.copy(loadingBtn = key) }

    private fun clearLoading() =
        _uiState.update { it.copy(loadingBtn = ButtonKey.NONE) }

    private fun showToast(msg: String, color: Long) {
        val id = System.currentTimeMillis()
        _uiState.update { s -> s.copy(toasts = s.toasts + ToastData(id, msg, color)) }
    }

    private fun showError(msg: String) {
        showDialog(msg)
        showToast(msg, 0xFFF87171)
    }

    private fun showDialog(msg: String) =
        _uiState.update { it.copy(showDialog = true, dialogMessage = msg) }
}
