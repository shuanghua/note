---
toc: false
comments: true
title: Pixel刷机步骤
description: 刷机前提:手机必须已经解锁了 OEM 
tags:
  - 手机
id: 31
categories:
  - 手机
date: 2018-2-20
---

#  一 、解锁 OEM（数据线 + 电脑 + ADB环境）

上锁和解锁都会清除手机数据，请提前做好数据备份（刷完机后建议请先上锁再恢复数据，以免后上锁会清除刚刚设置好的数据）

- 1.在手机开发者选项上打开 USB调试 和打开 OEM解锁 两个选项

- 2.通过数据线将手机连接至电脑（确保电脑具有 ADB 环境）

- 3.shift + 右键 选择 在此处打开 Powershell 窗口

- 4.输入 adb reboot bootloader 回车，等待手机进入到 bootloader 模式

- 5.电脑上继续输入 fastboot oem unlock 回车进行解锁（上锁请输入 fastboot oem lock）

- 6.然后用手机音量键选择到 unlock 选项（上锁请选 lock 选项），然后按手机开关机键 进行确定

- 7.等待手机自动重启，完成 bootloader 解锁/上锁（开机时屏幕下方出现一把打开的小锁，说明解锁成功。）


# 二、 刷机

刷机必须在 bootloader 模式下刷，并且 OEM 已经解锁。

- 1.下载 Android 镜像文件，然后解压。

- 2.进入 bootloader 模式有两种方式，一是先用数据线连接具备 ADB环境 的电脑，然后在电脑上命令输入：adb reboot bootloader ；二是在手机关机状态下，先不用数据线连接电脑，同时按住 电源键 + 音量减键 10秒。

- 3.进入到 bootloader 模式后，插上数据线，已经连接了数据就不要重复插了。

- 4.解锁 OEM （刷机必须，第一步已经完成就可以跳过这一步）

- 5.利用命令进入到镜像的解压目录，已经在此目录就跳过看下一步。

- 6.输入  ./flash-all.bat  回车

- 7.等待刷机完成，没问题的情况下刷机过程挺快的，最麻烦的还是刷机后还原数据的折腾。