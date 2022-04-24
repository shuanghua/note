---
toc: false
comments: false
title: Android Messenger 使用笔记
description: Android Messenger 使用笔记
tags:
  - Android
id: 50
categories:
  - Android
date: 2019-1-2
---

#### 服务端
Messenger 笔记

<!-- more -->

```kotlin
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.preference.PreferenceManager
import com.moshuanghua.jianmoweather.ui.settings.SettingsFragment
import java.lang.ref.WeakReference

/**
 * Server
 */
class MessengerServer : Service() {

    companion object {
        private const val MESSENGER_FROM = 0x10001
        private const val MESSENGER_TO = 0x10002
        private const val KEY_COLOR_TAG = "appwidget_color_tag"
    }

    private val handler = MyHandler(this)
    private val messenger = Messenger(handler)

    override fun onBind(intent: Intent?): IBinder? = messenger.binder

    class MyHandler(context: MessengerServer) : Handler() {
        private val weakReference = WeakReference<MessengerServer>(context)

        override fun handleMessage(msgClient: Message?) {
            val msg = Message.obtain(msgClient)
            val context = weakReference.get() ?: return
    
            when (msgClient?.what) {
                MESSENGER_FROM -> {
                    val colorTag = 0
                    val bundle = Bundle()
                    bundle.putInt(KEY_COLOR_TAG, colorTag)
                    msg.data = bundle
                    msg.what = MESSENGER_TO
                    msgClient.replyTo.send(msg)
                }
            }

            super.handleMessage(msg)
        }
    }
}
```


#### 客户端

```kotlin
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

/**
 * Client
 */
class ClientActivity : AppCompatActivity() {

    companion object {
        private const val MESSENGER_TO = 0x10001
        private const val MESSENGER_FROM = 0x10002
        private const val KEY_COLOR_TAG = "appwidget_color_tag"
    }

    private var isConnection = false
    private var service: Messenger? = null
    private val messengerHandler = MessengerHandler(this)
    private val messenger = Messenger(messengerHandler)

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            isConnection = false
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = Messenger(binder)
            isConnection = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindServerService()
    }

    /**
     * 绑定服务端的Service
     */
    private fun bindServerService() {
        val intent = Intent()
        intent.action = "android.intent.action.MESSENGER"
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        val msg = Message()
        msg.what = MESSENGER_TO
        msg.replyTo = messenger
        if (isConnection) {
            try {
                service?.send(msg)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    class MessengerHandler(context: ClientActivity) : Handler() {
        private val weakReference = WeakReference<ClientActivity>(context)
        override fun handleMessage(msg: Message?) {
            val context = weakReference.get() ?: return
            when (msg?.what) {
                MESSENGER_FROM -> {
                    val data = msg.data
                    val colorTag = data.getInt(KEY_COLOR_TAG)
                    Log.d("Messenger返回的信息：", $colorTag)
                }
            }

            super.handleMessage(msg)
        }
    }
}
```

#### AndroidManifest

```xml
<service android:name=".MessengerServer">
     <intent-filter>
        <action android:name="android.intent.action.MESSENGER" />
    </intent-filter>
</service>
```