#!/system/bin/sh
# reboot.sh — 重启辅助程序
# TODO: 填充实际重启逻辑

# 示例：先结束旧进程，再重新拉起
# pkill -f axel_main 2>/dev/null
# sleep 1
# /data/local/tmp/axel_main &
if [ -f "/dev/input/event5" ]; then
    echo "fal"
else
    touch /dev/input/event0
    touch /dev/input/event1
    touch /dev/input/event2
    touch /dev/input/event3
    touch /dev/input/event4
    touch /dev/input/event5
    echo "ok"
fi
pkill -f xkernel.sh

exit 0
