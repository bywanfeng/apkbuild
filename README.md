# 晚风工作室 · 三角洲启动程序

Android 本地启动器，为云手机环境设计，需要 ROOT 权限。

---

## 项目结构

```
app/src/main/
├── java/com/wanfeng/launcher/
│   ├── MainActivity.kt              # 入口 Activity
│   ├── service/
│   │   ├── RootUtil.kt              # 所有 su 调用封装
│   │   ├── NotificationUtil.kt      # 通知 Channel + 发送
│   │   ├── AssetUtil.kt             # assets 读取/提取
│   │   └── SimpleVpnService.kt      # VPN 服务占位
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Type.kt
│       │   └── Theme.kt
│       └── screen/
│           ├── LauncherViewModel.kt # 状态机 + 业务逻辑
│           └── LauncherScreen.kt    # Compose UI
├── assets/
│   ├── 公告.txt                     # 启动公告（本地读取）
│   ├── guli.txt                     # 游戏引言库（随机读取）
│   ├── bypass.zip                   # 占位，需手动替换
│   ├── fonts/
│   │   ├── HarmonyOS_Sans_SC_Medium.ttf
│   │   └── HarmonyOS_Sans_SC_Regular_blackmode.ttf
│   └── shell/
│       ├── fucktmp.sh               # 启动时静默执行
│       ├── run.sh                   # 启动辅助
│       ├── reboot.sh                # 重启辅助
│       ├── clear.sh                 # 清理防盗号
│       └── stop.sh                  # 完全关闭
└── jniLibs/arm64-v8a/
    └── libaxel.so                   # 占位，需手动替换
```

---

## 需要手动填充的文件

| 文件 | 说明 |
|------|------|
| `assets/bypass.zip` | 实际 bypass 包，替换占位文件 |
| `jniLibs/arm64-v8a/libaxel.so` | 实际 so 文件 |
| `assets/shell/*.sh` | 根据实际业务填充脚本内容 |

---

## GitHub Actions 编译

### Debug APK（无需配置，push 即编译）
直接 push 到 main/master 分支，Actions 自动编译 Debug APK。

### Release APK（需配置签名 Secrets）
在仓库 Settings → Secrets and variables → Actions 中添加：

| Secret | 说明 |
|--------|------|
| `KEYSTORE_BASE64` | keystore 文件的 Base64 编码 (`base64 -w0 my.jks`) |
| `KEYSTORE_PASSWORD` | keystore 密码 |
| `KEY_ALIAS` | key alias |
| `KEY_PASSWORD` | key 密码 |

---

## 本地编译

```bash
./gradlew assembleDebug
# APK 输出：app/build/outputs/apk/debug/
```

---

## 版本要求

- minSdk: Android 10 (API 29)
- targetSdk: Android 12 (API 31)
- 编译 JDK: 17
- Kotlin: 2.0.0 / Compose BOM: 2024.08.00
# apkbuild
# apkbuild
