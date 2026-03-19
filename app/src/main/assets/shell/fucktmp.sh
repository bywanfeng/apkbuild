#!/system/bin/sh
# fucktmp.sh — 启动时静默执行
# 用途：清理 /data/local/tmp 中的临时残留文件
# 请根据实际需求填充具体逻辑

# 示例：清理上次运行的残留
rm -rf /data/local/tmp/core* 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ace_shell_di.dat 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/tdm_tmp 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/shell_cache 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/com.gcloudsdk.gcloud.gvoice 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ano_tmp 2>/dev/null
exit 0
