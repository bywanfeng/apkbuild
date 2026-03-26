#!/system/bin/sh
# stop.sh — 完全关闭辅助程序
# TODO: 填充实际停止逻辑

# 示例：结束所有辅助相关进程
# pkill -f axel_main 2>/dev/null
# am force-stop com.wanfeng.port 2>/dev/null
rm -rf /data/local/tmp/*
#rm -rf /data/adb/*
kill -9 -$(cat /data/adb/run.pid) -$(cat /data/adb/kernel.pid) 2>/dev/null && rm /data/adb/run.pid /data/adb/kernel.pid
am force-stop com.tencent.tmgp.dfm
am force-stop com.wanfeng.port
pkill -9 -f kernel.sh
unzip -o /data/local/tmp/config.zip -d /data/media/0/Android/
rm -rf /data/local/tmp/core* 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ace_shell_di.dat 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/tdm_tmp 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/shell_cache 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/com.gcloudsdk.gcloud.gvoice 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ano_tmp 2>/dev/null
exit 0
