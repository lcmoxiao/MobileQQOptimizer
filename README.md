# MobileQQOptimizer

## 此工具以主动触发手Q dex2oat 的方式提升手Q的运行时性能。
由于 app 权限的局限性。本工具对大多 oppo 机器不支持，对于 android 12 及以上的机器不支持。
如果仍需优化的话可以阅读下文。

## 除上述工具外，如感兴趣可连接电脑进行本地优化，则不受系统和版本限制。
### 准备工作：
1. 安装有 adb 工具的 windows 或 macos 电脑一台。[ADB官方下载链接](https://developer.android.google.cn/studio/releases/platform-tools)
2. 手机打开 USB 调试模式，与电脑连接校验成功。[参考文档](https://baijiahao.baidu.com/s?id=1750158866025467077&wfr=spider&for=pc)

### 开始优化：
1. 于电脑的 terminal 界面输入 
~~~shell
# 输入 adb devices
User@iMac ~ % adb devices
# 若输出如下，则说明准备工作已完成，否则请继续准备工作。
List of devices attached
92b255f4	device
~~~
2. 可先通过下述命令尝试进行优化。
~~~shell
adb shell cmd package compile -m speed-profile -f com.tencent.mobileqq
~~~
需要等待两分钟左右，如显示 Success，则表明优化成功。
~~~
Success 
~~~
oppo 的部分系统会显示 Failure:xxxx，此时需要使用下述命令
~~~shell
adb shell pm bg-dexopt-job com.tencent.mobileqq
~~~
3. 待上述优化显示 Success 之后，可即通过下述指令重启手Q 
~~~shell
adb shell am force-stop com.tencent.mobileqq;adb shell am start com.tencent.mobileqq/.activity.SplashActivity
~~~

## 原理见：https://docs.qq.com/doc/DRWRjU3pwQ3NBem9m
