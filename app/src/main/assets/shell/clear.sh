#!/system/bin/sh
# clear.sh — 清理防盗号
# TODO: 填充实际清理逻辑（清除设备标识缓存等）

# 示例：清理游戏相关缓存
# pm clear com.tencent.tmgp.dfm 2>/dev/null
rm -rf /data/local/tmp/*
rm -rf /data/adb/*
kill -9 -$(cat /data/adb/run.pid) -$(cat /data/adb/kernel.pid) 2>/dev/null && rm /data/adb/run.pid /data/adb/kernel.pid
am force-stop com.tencent.tmgp.dfm
am force-stop com.wanfeng.port
rm -rf /data/local/tmp/core* 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ace_shell_di.dat 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/tdm_tmp 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/shell_cache 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/com.gcloudsdk.gcloud.gvoice 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ano_tmp 2>/dev/null
pm uninstall com.tencent.mm
pm uninstall com.tencent.mobileqq
/data/local/tmp/清理.sh 2>/dev/null
exit 0
