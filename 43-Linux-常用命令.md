---
toc: false
comments: false
title: 我的常用命令笔记
description:  我的常用命令笔记
tags:
  - Linux
id: 43
categories:
  - Linux
date: 2018-7-28
---

记录偶尔会用到的的一些命令
<!-- more -->


# Android Studio:
```bash
 ./studio.sh
```

# 权限
```
sudo chmod 777 * -R
sudo chmod 777
```


# 查看
dpkg -L xxxx //查看软件安装目录

查看软件版本
```bash
aptitude show xxx
apt-show-versions (要先安装sudo apt-get install apt-show-versions)
```


# 卸载
```bash
sudo apt-get remove xxx
```


# git 代理
```
repo init -u https://aosp.tuna.tsinghua.edu.cn/platform/manifest $gitdaili
```


# Ubuntu 配置 AndroidStudio 真机调试
- 输入 lsusb 查看当前连接手机的的设备供应商ID（ID 后面的四位数）
Bus 004 Device 008: ID 18d1:4ee7 Google Inc. 【Google 18d1】
```bash
lsusb
```

005: ID 12d1:1051 Huawei Technologies Co., Ltd.【华为 12d1】

- 添加真机连接配置规则：
```
vim /etc/udev/rules.d/51-android.rules
```

- 将以下内容放入: 
```
SUBSYSTEM=="usb", ATTR{idVendor}=="您设备的供应商ID", MODE="0666", GROUP="plugdev"
```
如 googel 手机的 Id 一般为 18d1 将上面的设备ID替换成 18d1，然后保存；


- 最后修改执行权限：
```
chmod a+r /etc/udev/rules.d/51-android.rules
```


# 网易云音乐启动
```
netease-cloud-music --no-sandbox %U  
```


# Pixel 黑域服务启动
```
adb -d shell sh /data/data/me.piebridge.brevent/brevent.sh
```


# AndroidStudio 模拟器权限问题
```bash
sudo apt install qemu-kvm
sudo adduser 当前系统的用户名 kvm
sudo chown 当前系统用户名 /dev/kvm
```
可能需要重启


#### 查看kpat运行依赖的具体版本
```
gradlew app:dependencies --configuration kapt
```

# ArchLinux Chrome 
```bash
git clone https://aur.archlinux.org/google-chrome.git
cd google-chrome/
makepkg -s
ls *.xz   //google-chrome-70.0.3538.77-1-x86_64.pkg.tar.xz
sudo pacman -U --noconfirm google-chrome-70.0.3538.77-1-x86_64.pkg.tar.xz
google-chrome-stable
```

# 安装 Golang 

> root 下安装

先点击下面连接查看 Go 的最新版本，我当前最新为1.12.5

[https://golang.org/dl/](https://golang.org/dl/ "xx")

- 下载安装
```bash
cd ~ && curl -O https://dl.google.com/go/go1.12.5.linux-amd64.tar.gz
```

- 解压
```bash
tar -C /usr/local -xzf go1.12.5.linux-amd64.tar.gz
```

- 添加到环境变量
```bash
vim ~/.bash_profile
```

- 添加以下两行内容到文末:
```
export GOPATH=$HOME/work
export PATH=$PATH:/usr/local/go/bin:$GOPATH/bin
```

- 使环境变量生效
```bash
source ~/.bash_profile
```

- 检查版本
```bash
go version
```


# AndroidStudio 获取 SHA1
- 进入 .android 文件夹
```bash
cd .android
```

- 然后输入 然后复制 SHA1
```bash
keytool -list -v -keystore debug.keystore
```