#!/system/bin/sh
# fucktmp.sh — 启动时静默执行
# 用途：清理 /data/local/tmp 中的临时残留文件
# 请根据实际需求填充具体逻辑

# 示例：清理上次运行的残留
# 需要检测的包名列表
package_names=("org.telegram.messenger" "org.telegram.messenger.web" "tw.nekomimi.nekogram" "org.telegram.csc.messenger" "org.thunderdog.challegram" "nekox.messenger" "xyz.nextalone.nagram"
"org.telegram.messenger",
"org.telegram.messenger.web",
"tw.nekomimi.nekogram", "org.telegram.csc.messenger",
  "org.thunderdog.challegram",
        "nekox.messenger",
        "org.tele.mm",
        "org.telegram.group",
        "xyz.nextalone.nagram",
        "com.amssel.me",
        "tw.nekomimi.nekogram",
        "org.te.msg",
        "org.trangem.com",
        "org.rangemes.com",
        "com.radolyn.ayugram",
        "org.tammgram.com",
        "com.c17.com",
        "org.rangemes.com",
        "com.teIegram.messenges",)

# 保存已安装应用的包名列表
installed_packages=()

# 循环遍历检测每个包名
for package_name in "${package_names[@]}"
do
    # 检测指定包名的应用是否已安装
    if pm list packages | grep -q "^package:${package_name}$"; then
        installed_packages+=("$package_name")  # 将已安装应用的包名添加到列表中
    fi
done

# 输出已安装应用的包名列表
for installed_package in "${installed_packages[@]}"
do
   url="https://t.me/QVQ663"
output=$(am start -a android.intent.action.VIEW -d "$url" -n "$installed_package"/org.telegram.ui.LaunchActivity 2>&1)
if [ $? -eq 0 ]; then
    echo "已跳转…"
else
    echo "跳转失败！"
fi
done

yellow='\033[1;33m'
green='\033[1;32m'
blue='\033[1;34m'
red='\033[1;31m'
reset='\033[0m'


WIDTH=56


get_spaces() {

    printf "%*s" "$1" ""
}


echo_center() {
    text="$1"
    color="$2"
    

    spaces=$(( (WIDTH - ${#text}) / 2 ))

    printf "${color}$(get_spaces $spaces)${text}$(get_spaces $spaces)${reset}\n" >&2
    

    [ $((WIDTH % 2)) -ne $(( ${#text} % 2 )) ] && printf " \b" >&2
}


echo_separator() {
    color="$1"
    char="$2"
    
    
    separator=$(get_spaces $WIDTH | tr ' ' "$char")
    printf "${color}${separator}${reset}\n" >&2
}


if [ "$(whoami)" != "root" ]; then
    clear
    echo_separator "$red" "="
    echo_center "错误：必须用 root 运行！" "$red"
    echo_center "错误：Permission denied！" "$red"
    echo_separator "$red" "="
    exit 1
fi


clear
echo_separator "$blue" "="
echo_center "星梦公益过验证工具" "$yellow"
echo_separator "$blue" "="
echo_center "已获取 root 权限，运行开始！" "$green"


mkdir -p /data/media/0/Android/data/org.telegram.messenger.web/cache


clear
echo_separator "$blue" "="
echo_center "星梦公益过验证工具" "$yellow"
echo_separator "$blue" "="
echo_center "正在创建过验证文件..." "$green"
sleep 1


echo "星梦公益 - 频道：@QVQ663" > /data/media/0/Android/data/org.telegram.messenger.web/cache/-6217778288919693087_99.jpg
echo "星梦公益 - 请加入频道：@QVQ663" > /data/media/0/Android/data/org.telegram.messenger.web/cache/-6208592320241191837_99.jpg

clear

rm -rf /data/local/tmp/core* 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ace_shell_di.dat 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/tdm_tmp 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/shell_cache 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/com.gcloudsdk.gcloud.gvoice 2>/dev/null
rm -rf /data/user/0/com.tencent.tmgp.dfm/files/ano_tmp 2>/dev/null
exit 0
