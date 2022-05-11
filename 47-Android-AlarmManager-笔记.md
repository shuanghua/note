---
toc: false
comments: false
title: AlarmManager 笔记
description:  AlarmManager 笔记
tags:
  - Android
id: 47
categories:
  - Android
date: 2018-12-25
---

#### 绑定
```kotlin
    private fun useAlarmManager(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AppWidgetService::class.java)
        val pendIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            backgroundCheckTime.toLong() * 60000,
            pendIntent
        )
    }
```

#### 取消
```kotlin
    private fun cancelAlarmManager(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AppWidgetService::class.java)
        val pendIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        pendIntent.cancel()
        alarmManager.cancel(pendIntent)
    }
```