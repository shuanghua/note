---
toc: false
comments: false
title: Android 解锁屏广播监听笔记
description: Android 解锁屏广播监听
tags:
  - Android
id: 49
categories:
  - Android
date: 2019-1-1
---

#### 广播回调
```kotlin
    private val screenBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action == null) {
                return
            }
            if (intent.action == Intent.ACTION_SCREEN_OFF) {//黑屏
                pauseWorking()//暂停相关工作
            } else if (appWidgetExisted && intent.action == Intent.ACTION_SCREEN_ON) {//屏幕亮时（此时可能是锁屏界面）
                if (!keyguardManager.isKeyguardLocked) {//并且用户没有设定锁屏密码
                    restartWorking()
                }
            } else if (appWidgetExisted && intent.action == Intent.ACTION_USER_PRESENT) {//有锁屏密码，且用户解锁了
                restartWorking()
            }
        }
    }
```

#### 注册广播
```kotlin
    private fun setUpBroadcast() {
        val screenBroadcastIntentFilter = IntentFilter()
        screenBroadcastIntentFilter.apply {
            addAction(Intent.ACTION_SCREEN_ON)//亮屏
            addAction(Intent.ACTION_SCREEN_OFF)//熄屏
            addAction(Intent.ACTION_USER_PRESENT)//解锁
            priority = 999
        }
        registerReceiver(screenBroadcastReceiver, screenBroadcastIntentFilter)
    }
```