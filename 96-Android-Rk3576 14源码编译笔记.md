作者：SamMo
# 了解 Aosp
aosp 的代码是包含多个设备类型及编译产物，如 x86, arm , debug, usedebug 还有各个厂商的等, 不同的设备使用驱动不同，对于真实设备，我们通常需要手动去 google 官网上下载对应的驱动，然后放到 aosp 中，然后编译，选择要启动的设备， 这样编译脚本才能知道去引入哪些配置

> 对于定制的开发版，通常就需要自己提供：device kernel hardware uboot 之类的

以下针对定制基于 rk3576 的开发版进行编译


# 编译
### 1.设置构建环境
```
source build/envsetup.sh    // 没有错误的话，通常很快执行完成
```


### 2.选择基于某个设备编译
```
lunch      // 选择基于某个设备来编译  rk3576_u-userdebug
```


### 3.开始编译
执行根目录 build.sh 这个脚本会基于前面选择的设备来进行编译，包括编译对应 uboot kernel device android 等，最终各个镜像文件
```
./build.sh -UKA
```


### 4.打包成一个镜像文件
通常可以利用 rktools 里面的打包脚本进行打包，但这个固件中好像没有找到 rk3576的打包脚本，打包好后，通常生成在 `rockdev/Image-rk3576_u` 目录里面
```
cd RKTools/linux/Linux_Pack_Firmware/rockdev
./mkupdate.sh -l ~/work/rockchip/android_u/rk3576_v2/rockdev/Image-rk3576_u
```

也可以直接使用 build.sh 打包，只需要加上对应的参数即可
```
./build.sh -UKAu

```


> 如果 U K 这两步编译成功，但在编译 Android 的过程出错，作为调试(不烧录)，减少编译时间，后续只要执行 ./build.sh -A 即可




# 遇到的错误
### ❌错误1
重复的 ATVOverlay ，解决：注释掉其中一个 Android.bp 里面的内容
```
error: vendor/gapps_tv/overlay/ATVOverlay/Android.bp:1:1: module "ATVOverlay" already defined

packages/apps/vendor_gapps_tv-tau/overlay/ATVOverlay/Android.bp:1:1 <-- previous definition here
```


# 附上完整编译流程：
```
source build/envsetup.sh
lunch rk3576_u-userdebug
./build.sh -UKAu
```
自行调整对应的编译参数（更多的参数介绍可以在根目录的 build.sh 中查看）
比如 J 就是编译线程数 （build.sh 默认 BUILD_JOBS = 16 线程）


# 一键编译脚本
```
./mk_rk3576.sh
```





















