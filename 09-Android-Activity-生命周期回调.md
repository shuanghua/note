---
toc: true
comments: true
description: Activity 生命回调
title: Activity 生命回调-笔记
tags:
  - Android
id: 11
categories:
  - Android
date: 2017-4-3
---

# 正常启动
1. onCreate
2. onStart
3. onPostCreate
4. onResume
5. onPostResume


<!-- more -->

# 按任务列表键
1. onPause
2. onStop 
## 滑掉销毁 (不会再回调任何生命方法)
1. onPause （按任务列表键时的方法）
2. onStop （按任务列表键时的方法）
## 不销毁，点击按任务键返回（后台单应用）
1. onRestart
2. onStart
3. onResume
4. onPostResume
## 不销毁，点击按任务键切换到别的应用（后台双应用）
1. onPause
2. onStop 
### 再点击切换回来
1. onRestart
2. onStart
3. onResume
4. onPostResume

# 按返回键返回
## 单 Activity （或者栈中只剩当前一个 Activity）
1. onPause
2. onStop
3. onDestroy

## 多 Activity
### 跳转没有 finish
1. onPause
2. （新 Activity 正常启动，看正常启动的方法） 
3. onStop
### 跳转后 finish
1. onPause
2. （新 Activity 正常启动，看正常启动的方法） 
3. onStop
4. onDestroy
#### 跳转后 finish，按返回键返回 （这里用三个 Activity 举例）
- （两个 Acitvity 下按返回的情况请看：栈中只剩当前一个 Activty 情况。）

- 假设：三个 Activity 分别为： A B C ，其中 B 在跳转后设置 finish。

当从在第三个 Activity 按返回时：
onPause （C）

onRestart (A)
onStart (A)
onResume (A)
onPostResume (A)

onStop (C)
onDestroy (C)

# 屏幕旋转

- 假设最开始为竖屏,要转为横屏

先:

1. onPause (竖屏)
2. onStop (竖屏)
3. onDestroy (竖屏)

旋转完成:

相当重新 "**正常启动一个 Activity**" 


1. onCreate (横屏)
2. onStart (横屏)
3. onPostCreate (横屏)
4. onResume (横屏)
5. onPostResume (横屏)

再旋转回来,以上两步的方法重新走一遍,只不过横竖对象换过来了.
