#!/system/bin/sh
# run.sh — 启动辅助程序
# TODO: 填充实际启动逻辑（调用 libaxel.so 或其他可执行文件）

# 示例：启动位于 /data/local/tmp 的辅助主程序
# /data/local/tmp/axel_main &

# 杀进程
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
echo -e "1\n1\n" | /data/local/tmp/run.sh >> /data/adb/bypass.log 2>&1 &
echo -e "2\n1\n" | /data/local/tmp/xkernel.sh >> /data/adb/kernel.log 2>&1 &

exit 0
