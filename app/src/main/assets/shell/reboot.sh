#!/system/bin/sh
# reboot.sh — 重启辅助程序
# TODO: 填充实际重启逻辑

# 示例：先结束旧进程，再重新拉起
# pkill -f axel_main 2>/dev/null
# sleep 1
# /data/local/tmp/axel_main &
kill -9 -$(cat /data/adb/run.pid) -$(cat /data/adb/kernel.pid) 2>/dev/null && rm /data/adb/run.pid /data/adb/kernel.pid
am force-stop com.tencent.tmgp.dfm
pkill -9 -f kernel.sh

# 新建触摸层
if [ -f "/dev/input/event5" ]; then
    echo "run"
else
    touch /dev/input/event0
    touch /dev/input/event1
    touch /dev/input/event2
    touch /dev/input/event3
    touch /dev/input/event4
    touch /dev/input/event5
    echo "ok"
fi

# 运行前环境部署
chmod 777 /data/local/tmp/kernel.sh
chmod 777 /data/adb/tmp/librun.so
chmod 777 /data/local/tmp/清理.sh

# 过检并且开启辅助
cd /data/local/tmp
/data/adb/tmp/librun.so >> /data/adb/bypass.log 2>&1 &
echo $! > /data/adb/run.pid
echo -e "2\n1\n" | nohup /data/local/tmp/kernel.sh >> /data/adb/kernel.log 2>&1 &
echo $! > /data/adb/kernel.pid
sleep 5
rm -rf /data/local/tmp/*
#rm -rf /data/adb/*
exit 0
