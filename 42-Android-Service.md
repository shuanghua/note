---
toc: false
comments: true
title: Android 8.0 + 的 Service 使用笔记
description: 在 Android 8.0 及以后，Google 对 Service 进行了诸多的改进，以限制开发者滥用 Service.现在的 Service 和 应用本身的联系更加的紧密。所以先来了解一下前台应用和后台应用
tags:
  - Android
id: 42
categories:
  - Android
date: 2018-7-28
---


# 三种 Service 类型：
- startService
- bindService
- Scheduled Service 计划服务,例如：JobScheduler.

<!-- more -->

# Android 8.0- / Android O- / API 26-
在 Android 8.0 之前，我们要创建一个 "前台" Service ，通常是先 startService() 启动一个后台 Service，然后调用 startForeground() 将该服务提升为前台 Service。


# Android 8.0+ / Android O+ / API 26+
在 Android 8.0 及以后，Google 对 Service 进行了诸多的改进，以限制开发者滥用后台 Service.现在的 Service 和 应用本身的联系更加的紧密。所以先来了解一下前台应用和后台应用：

应用处于前台的条件：
- 有一个可见的 Activity
- 有一个前台 Service 正在运行
- bandService 正在和一个别的前台应用连接
- ContentProvider 正在和一个别的前台应用连接
- 存在通知
- 接收存在 FCM 消息
- 手动或系统开机后自动向桌面添加微件时，此时会识别为一段时间的前台应用，添加完成后，前台应用可能会因为系统强杀导致变为后台应用（国内 rom 表现的比较多，原生不会）

如果上述条件满足，则系统会自动识别我们的应用为前台应用（系统可能缓存该应用进程），否则识别为后台应用（系统不会为后台应用缓存任何进程和服务）。

> 当我们的应用被系统识别为后台应用时，此时不能调用 startService 来启动任何有一个服务，但 bindService 例外。同样的,当应用被系统杀死处于后台应用时，AlarmManager 也启动不了 Service ;所以在8.0+ 又是国内的rom,还是不要尝试这种方式保活服务。


## 举个例子：
假如：当我们有一个繁重的任务，我们必须在应用处于后台状态下开启一个前台 Service 开处理我们的任务，往往这类型的任务处理过程中会占用大量内存，而当用户在玩游戏，此时可能就会导致内存紧张或者 CPU 忙不过来，最终用户感受到卡顿，这是非常糟糕的现象。像这样的场景残害了 Android 用户许多年；直到 Android 8.0 开始， Google 开始整治这种后台摧残手机系统性能的现象，并推荐使用 JobScheduler（JobScheduler 会自动在系统空闲的时候去处理该任务），所以别在 TMD 瞎搞。当然 Google 也不是一刀切死，如果不用 JobScheduler ,而是继续使用 Service ，则 必须在通知栏上显示一个通知来让用户知道：你的应用正在后台干坏事 <0.0>
当然对于非常繁重的任务，且需要通过 Service 来辅助完成，还是优先推荐在前台 Service 中处理，然后再考虑 JobScheduler。

- 那么如何在 Android 8.0+ 创建一个前台 Service 呢？
答：必须使用 service.startForeground(int id, Notification notification)，我们必须先创建通知面板，这么创建 Service ,系统会在通知栏上显示类似得："xxxxx应用正在后台运行" , 以让用户明确的知道我们应用关闭后依然有服务在运行。这种方式有一点要注意：当你调用 startService（）或者 startForegroundService 来启动 Service 后，必须在5 秒时间内调用 service.startForeground(int id, Notification notification) 来将这个服务提升为前台服务 ，如果超过了这个时间，那么抱歉，一个 IllegalArgumentException：Not allowed to start service Intent 依然送给你。这样启动一个服务的前提还是首先应用本身出于前台应用。

- 附上使用代码：
```kotlin
class MyForeBroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
	// 必须用通知来将 Service 提升为前台 Service
        customNotification("", 55)

	//doWork() 执行程序关闭退出后的任务

        return super.onStartCommand(intent, flags, startId)
    }

    private fun customNotification(title: String, notificationID: Int) {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* request code */,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(this, title)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(title,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager.createNotificationChannel(channel)
        }

        val notification = builder.build()
        startForeground(notificationID, notification)
    }

    companion object {
        private const val TAG = "MyForeBroundService"
    }
}
```

# JobIntentService
在上述的例子中说到，除了用前台 Service 来处理任务，我们还可使用 JobScheduler ，JobIntentService 是 Service 的子类，和 JobScheduler 的工作方式类似，都是当系统处于空闲时才去执行相应的任务。与原来的 Service 不同的是：JobIntentService 可以在任何时间启动，而什么时候结束就得看手机系统 “安排！”。


```kotlin
class DesktopWidgetService : JobIntentService() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleWork(intent: Intent) {
        //此处是子线程
	val weather = getNetWorkData()
	updataWidget(weather)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun getNetWorkData():Weather {
	//任务代码
    }

    private fun updataWidget(weather:Weather){
	MyAppWidgetProvider.updataAppWidget(weather)
    }

    companion object {
        //一个 Service 对应一个 id
        private const val JOB_SERVICE_WIDGET_ID = 10111

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, DesktopWidgetService::class.java, JOB_SERVICE_WIDGET_ID, work)
        }
    }
```

上面代码：我们希望在应用推出后在后台下载天气数据，然后设置给我们的桌面小部件。

```kotlin
class DesktopAppWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
	//记得在 AndroidMainifest 添加该 Action
        if (intent?.action == ACTION_REFRESH) {
            DesktopWidgetService.enqueueWork(context!!, Intent())
        }
    }

    /**
     * 每添加一个本应用的桌面 Widget 都回调一次
     * xml 中的 updatePeriodMillis 时间到了，回调一次
     */
    override fun onUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        DesktopWidgetService.enqueueWork(context, Intent())
    }

    companion object {
        fun updateAppWidget(context: Context, data: Weather) {
            Timber.d(data.currentT)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, DesktopAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                val remoteViews = RemoteViews(context.packageName, R.layout.appwidget_desktop)
                
                val refreshIntent = Intent(context, DesktopAppWidgetProvider::class.java)
                refreshIntent.action = ACTION_REFRESH
                val refreshPendingIntent = PendingIntent.getBroadcast(context, 11, refreshIntent, 11)
                remoteViews.setOnClickPendingIntent(R.id.appwidget_refresh, refreshPendingIntent)

                remoteViews.setTextViewText(R.id.appwidget_t, data.currentT)
                remoteViews.setTextViewText(R.id.appwidget_station, data.cityName)
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
            }
        }

        private const val ACTION_REFRESH = "APPWIDGET_REFRESH"
    }
}
```


## 解决微件刷新思路
在桌面的小部件上，定义了一个刷新按钮，点击该按钮，发送一个广播(或者利用 AlarmManager 定时来发送广播)，然后在 onReceiver（）中接收该广播，最后调用 DesktopWidgetService.enqueueWork(context, Intent()) 来启动 JobIntentService，这里一定不能是普通的 Service ，同时把 xml/widget_config.xml 文件中的 android:updatePeriodMillis="0" 设置为 0，最后在 DesktopWidgetService（DesktopWidgetService 继承 JobIntentService） 中请求网络，刷新微件。缺点是可能不一定实时刷新，详细请自行查阅 JobIntentService。

# 总结
Service + 通知 也只能在应用出于前台的前提下去创建启动。
Service + 通知的形式，在原生系统上是能长时间保持前台服务的运行，但在国内有些 rom 的环境下并不能很好的工作，比如一个天气应用，桌面微件晚上睡觉前好好的，第二天一早，你的前台服务就被干死了，你的微件显示 的天气数据是半夜的，然后你还不能通过点击微件去重新启动，因为此时系统后台不存在与你应用任何相关的内存，也就是说你的应用已经是一个后台应用，你无法在后台启动创建服务。国内道高一尺魔高一丈，最受伤的还是老实人，所以实时刷新在国内略显尴尬！

JobIntentService 不具备实时性质，例如你要每分每秒更新UI，这不适合。
#### 综上所述
- 解决办法让用户把应用本身常驻后台，但这会增加内存的使用，而且应用带有自动网络刷新，还会增加耗电。
- 或者接入FCM (国内时灵时不灵的，看当地网络脸色行事)
- 接入国内流氓推送唤醒（其实在国内rom的强杀下，已经没什么用了，但用户不去主动强杀主流氓应用（不杀微信，淘宝，支付,qq 等应用）还是可以一试的）
- 等国内统一推送（时间未知？？？？）
- JobIntentService


> JobIntentService 还有一个痛点，JobIntentService 里面真的不好配合 Kotlin 协程。

























