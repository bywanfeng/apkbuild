# 已修复内容

本次仅处理 **编译兼容 / UI / 交互**，未改动业务脚本、root 调用、VPN 启动流程。

## 修复项

1. **Manifest 修复**
   - 删除了错误的 `uses-permission android.permission.BIND_VPN_SERVICE` 声明。
   - 保留了 `service` 上正确的 `android:permission="android.permission.BIND_VPN_SERVICE"`。

2. **通知权限兼容**
   - `MainActivity` 增加 Android 13+ `POST_NOTIFICATIONS` 运行时申请。
   - `NotificationUtil` 在没有通知权限时直接跳过通知发送，避免异常。

3. **UI 交互优化**
   - 任务执行期间禁用重复点击，避免多按钮连点造成状态错乱。
   - 主启动按钮增加禁用态文案。
   - 快捷操作卡片在执行期间显示禁用态文案。
   - 底部状态区增加“任务执行中”提示。
   - Toast 限制最大宽度并处理长文本溢出。
   - 底部版本号改为动态读取 `BuildConfig.VERSION_NAME`。

4. **资源兼容**
   - 额外保留 `公告.txt` 文件，避免压缩包跨平台解压后中文文件名异常。

## 建议

- 直接把这个工程推到 GitHub，用 Actions 云编译。
- 若后续你贴出 Actions 的具体报错日志，我可以继续按日志定点修。
