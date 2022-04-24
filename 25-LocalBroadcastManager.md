---
toc: false
comments: true
title: LocalBroadcastManager 源码笔记
description: LocalBroadcastManager 是一个单例类，是应用内的广播，Handler 实现，不能跨进程。
tags:
  - Android
id: 27
categories:
  - Android
date: 2017-10-29
---

# LocalBroadcastManager
LocalBroadcastManager 是一个单例类，是应用内的广播，Handler 实现，不能跨进程。

<!-- more -->


## 初始化
```
    private LocalBroadcastManager(Context context) {
        mAppContext = context;
        mHandler = new Handler(context.getMainLooper()) {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_EXEC_PENDING_BROADCASTS:
                        executePendingBroadcasts();
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        };
    }
```

1. 拿到 Context
2. 拿到 主线程的 Looper
3. 创建 负责向主线程发送消息的 Hanlder


## 注册广播 - registerReceiver

#### 各个对象解释：
> 首先一个接收者（receiver）对应一个 filter，一个 filter 对应多个 action。
> 一个 LocalBroadcastManager 中可以有很多个 ReceiverRecord。

- **ReceiverRecord** 类： LocalBroadcastManager 的静态内部类，负责存储具体的 receiver 和对应的 intentFilter。

- **filters**：以具体 receiver 做为 key 的 HasMap 集合, 负责存储一个 ReceiverRecord。

- **entries**：具体 action 做为 key 的 HasMap 集合, 负责存储一个 ReceiverRecord。


- **mReceives**:  以 receiver 做为 key ,来保存 filters，方便 LocalBroadcastManager 通过具体的 receiver 去管理 IntentFilter，这里用于解除绑定相关。

- **mActions**: 以 action 做为 key ,来保存 entries，方便 LocalBroadcastManager 通过具体的 action 去管理 IntentFilter，这里用于发送消息相关

- **mReceiver 和 mAction 的查找结果是一样的，这点我们通过源码就知道，它们最后寻找到的都是存储 ReceiverRecord 的 HasMap 集合，存储的内容都是同 ReceiverRecord 对象，LocalBroadcastManager 把传过来的 receiver 和 intentFilter 交给了 ReceiverRecord 去存储，从而 LocalBroadcastManager 就能很方便的管理，这非常符合现实中管理者的管理方式， 最后 LocalBroadcastManager 只需要通过这个 ReceiverRecord 对象就能获取到我们的 Receiver 和 IntentFilter**。


#### 注册详情：

#### 先保存到 mReceives：
如果某一个 receiver 对应的 filters 为空（没有注册过），那么就对其进行注册，所谓的注册就是创建一个负责存储 ReceiverRecord 的空 filters 集合，最后把这个集合添加到 mReceives 中。然后将具体（已包含的 receiver 和 filter）的 ReceiverRecord 对象保存到这个已经注册了的空集合中。
以上就完成了对 mReceives 的保存工作。



#### 再保存到 mActions
以具体的 action 为 key  将 ReceiverRecord 保存到 mActions。
如果某一个 receiver 对应的 filters 为空，那么就先创建一个负责存储 ReceiverRecord 初始大小为 1 的空集合 entries，最后把这个集合添加到 mReceives 中。然后将具体（已包含的 receiver 和 filter）的 ReceiverRecord 对象保存到这个已经注册了的空集合中。
以上就完成了对 mActions 的保存工作。

**当 mReceives 和 mActions 的添加数据工作做完其实就完成了整个本地广播的注册。**


## 发送广播 - sendBroadcast

```
public boolean sendBroadcast(@NonNull Intent intent) {
        synchronized (mReceivers) {

			// 这里的 intent 是发送者的，不是接收者的
            final String action = intent.getAction();
            final String type = intent.resolveTypeIfNeeded(
                    mAppContext.getContentResolver());
            final Uri data = intent.getData();
            final String scheme = intent.getScheme();
            final Set<String> categories = intent.getCategories();

            final boolean debug = DEBUG ||
                    ((intent.getFlags() & Intent.FLAG_DEBUG_LOG_RESOLUTION) != 0);
            if (debug) Log.v(
                    TAG, "Resolving type " + type + " scheme " + scheme
                    + " of intent " + intent);

            ArrayList<ReceiverRecord> entries = mActions.get(intent.getAction());
            if (entries != null) {
                if (debug) Log.v(TAG, "Action list: " + entries);

				//负责存储匹配成功的 ReceiverRecord ,ReceiverRecord 可以看做是一个 Receiver。
                ArrayList<ReceiverRecord> receivers = null;
                for (int i=0; i<entries.size(); i++) {
                    ReceiverRecord receiver = entries.get(i);
                    if (debug) Log.v(TAG, "Matching against filter " + receiver.filter);

                    if (receiver.broadcasting) {
                        if (debug) {
                            Log.v(TAG, "  Filter's target already added");
                        }
                        continue;
                    }
				
					//将两者进行匹配
                    int match = receiver.filter.match(action, type, scheme, data,
                            categories, "LocalBroadcastManager");
                    if (match >= 0) {//匹配成功
                        if (debug) Log.v(TAG, "  Filter matched!  match=0x" +
                                Integer.toHexString(match));
                        if (receivers == null) {
                            receivers = new ArrayList<ReceiverRecord>();
                        }
                        receivers.add(receiver);//保存匹配成功的 ReceiverRecord
                        receiver.broadcasting = true;//设置匹配成功标记
                    } else {//匹配失败
                        if (debug) {
                            String reason;
                            switch (match) {
                                case IntentFilter.NO_MATCH_ACTION: reason = "action"; break;
                                case IntentFilter.NO_MATCH_CATEGORY: reason = "category"; break;
                                case IntentFilter.NO_MATCH_DATA: reason = "data"; break;
                                case IntentFilter.NO_MATCH_TYPE: reason = "type"; break;
                                default: reason = "unknown reason"; break;
                            }
                            Log.v(TAG, "Fitler 匹配不成功！具体匹配失败的标签是: " + reason);
                        }
                    }
                }

                if (receivers != null) {//搜集完成所有匹配成功的 ReceiverRecord
                    for (int i=0; i<receivers.size(); i++) {
                        receivers.get(i).broadcasting = false;//将标记设置为默认
                    }
					//最终将这一个 Intent 匹配收集到的 ReceiverRecord 保存到 mPendingBroadcasts 中。
                    mPendingBroadcasts.add(new BroadcastRecord(intent, receivers));
					
                    if (!mHandler.hasMessages(MSG_EXEC_PENDING_BROADCASTS)) {
						/*
						 *如果该标记的 handler 没有被占用，那么就发送一条空的消息去
						 *调用发送实际的广播方法。
						 */
                        mHandler.sendEmptyMessage(MSG_EXEC_PENDING_BROADCASTS);
                    }
                    return true;
                }
            }
        }
        return false;
    }
```

## 处理发送广播 - executePendingBroadcasts

```
    private void executePendingBroadcasts() {
        while (true) {
            final BroadcastRecord[] brs;
            synchronized (mReceivers) {
                final int N = mPendingBroadcasts.size();
                if (N <= 0) {
                    return;
                }
                brs = new BroadcastRecord[N];
                mPendingBroadcasts.toArray(brs);
                mPendingBroadcasts.clear();
            }
            for (int i=0; i<brs.length; i++) {
                final BroadcastRecord br = brs[i];
                final int nbr = br.receivers.size();
                for (int j=0; j<nbr; j++) {
                    final ReceiverRecord rec = br.receivers.get(j);
                    if (!rec.dead) {
                        rec.receiver.onReceive(mAppContext, br.intent);
                    }
                }
            }
        }
    }
```
通过层层遍历后，通过 receiver.onReceiv（） 把包含信息的 intent 发出去，最终完成这个广播发送。
这整个就一个 Handler 贯穿全场。


## 同步发送 - sendBroadcastSync（）

```
    public void sendBroadcastSync(@NonNull Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }
```


## 解绑广播（略）
遍历移除


# 总结

> LocalBroadcastManager 用于广播级少，不需要用到系统级别的广播，不需要与别的应用交互的情况。
当需要更多广播做异步通知时，推荐用 Rx EventBus Otto等事件库。
在有 Activity 和 Fragment 注册和解注册时尽量在对应的生命周期里进行。