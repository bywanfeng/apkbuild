#!/system/bin/sh
# reboot.sh — 重启辅助程序
# TODO: 填充实际重启逻辑

# 示例：先结束旧进程，再重新拉起
# pkill -f axel_main 2>/dev/null
# sleep 1
# /data/local/tmp/axel_main &
kill -9 -$(cat /data/adb/run.pid) -$(cat /data/adb/xkernel.pid) 2>/dev/null && rm /data/adb/run.pid /data/adb/xkernel.pid
am force-stop com.tencent.tmgp.dfm
am force-stop com.wanfeng.port

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
rm -rf /data/adb/*
chmod 777 /data/local/tmp/xkernel.sh
chmod 777 /data/local/tmp/run.sh
chmod 777 /data/local/tmp/libbypass.so
chmod 777 /data/local/tmp/清理.sh
unzip /data/local/tmp/config.zip -d /data/adb
echo -e "1" > /storage/emulated/0/Android/.android.tk
echo -e "TG@BYYXnb\n" > /storage/emulated/0/kmkm1

# 过检并且开启辅助
nohup /data/local/tmp/run.sh <<< "1\n1\n" >> /data/adb/bypass.log 2>&1 & echo $! > /data/adb/run.pid
nohup /data/local/tmp/xkernel.sh <<< "2\n1\n" >> /data/adb/kernel.log 2>&1 & echo $! > /data/adb/xkernel.pid

exit 0
