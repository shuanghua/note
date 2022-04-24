---
toc: true
comments: true
description: Android高级笔记
title: Android高级笔记
tags:
  - Android
id: 8
categories:
  - Android
date: 2016-5-19
---

# 一、在子线程能否更新UI？
> 在 ViewRoot 还没有被创建的时候，是可以在子线程中直接更新 UI。

- ViewRoot 在 onCreat 时没有创建
- ViewRoot 在 onResume 后才创建
- UI 线程是没有加锁的，在非 UI 线程更新 UI 时候是不安全的操作，所以 Android 不让在子线程中直接更新 UI。



<!-- more -->

# 二、IPC
> IPC： 进程之间的通信，Binder 是 Android 底层的进程间通信方式

- 在清单配置文件中通过应用 process 来指定进程 “：”说明是私有的进程（数据不共享）
- Binder 通过 Parcel 序列化来传递数据
- 简单的数据可以通过 Bundle 来或文件方式传递，但文件传递不及时，不安全。（解决：将文件的读写工作全部放到同一个进程中）


# 三、Binder 调用
- 同进程：返回 Stub (正常的方法调用)
- 不同进程：返回 BinderProxy


# 四、 自定义 Binder
1. 创建  AIDL 必须继承 IInterface，IInterface 负责判断是否是同一个进程，是同一个进程返回真实对象，不是同一个进程返回 Binder 代理对象。
2. 创建一个 Class 继承 IBinder 并实现 AIDL。


# 五、Binder 进程异常
- 有的异常 能 通过 Binder 进行跨进程返回，如 NullPointerException
- 有的异常 不能 通过 Binder 进行跨进程返回，如 RuntimeException
- 在服务端的异常不会导致服务端所在的进程挂掉
- 如果客户端收到异常，则客户端所在的进程会挂掉


# 六、Messenger
## 服务端进程
1. 创建一个 Service 类
2. 在 Service 类中 new 一个 Messenger（MessengerHandler） 负责接收消息
![](http://7xrysc.com1.z0.glb.clouddn.com/MyService.png)

## 客户端进程
1. 绑定 Service 
2. 通过 new Messenger(IBinder) 来构造一个 Messenger 对象
3. 通过 Message.obtain(Hander,flag)
4. Bundle 装数据，Message 装 Bundle
5. Messenger 对象发送 Message
![](http://7xrysc.com1.z0.glb.clouddn.com/send.png)

处理服务端返回的消息：

![](http://7xrysc.com1.z0.glb.clouddn.com/%E6%8E%A5%E6%94%B6%E8%BF%94%E5%9B%9E%E7%9A%84%E6%B6%88%E6%81%AF.png)