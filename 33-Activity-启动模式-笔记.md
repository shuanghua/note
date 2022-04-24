---
toc: false
comments: true
title: Activity 启动模式-笔记
description: Activity 启动模式个人笔记
tags:
  - Android
id: 33
categories:
  - Android
date: 2018-4-29
---


# Activity 启动模式
- **standard**：标准模式，每启动一个 Activity 不管该实例原来是否创建过，都会在栈中创建一个该 Activity 实例。

- **singleTop**: 栈顶复用模式，如果启动的 Activity 实例正处于当前栈顶时，就不会重新创建实例（此时 onNewIntent() 方法会被调用）；不在栈顶就创建该实例（不管该 task 中是否存在该实例，只要不是在栈顶，都会重新创建实例）

- **singleTask**: 栈内单例复用，创建新Task，将实例放入，同一个栈中只能有一个该 Activity 实例，但这个任务栈可以存放 非 singleInstance 的 Activity 实例, 当栈内已存在实例时，先把该实例前面别的所有实例移除出栈，以确保该实例处于栈顶位置（此时 onNewIntent() 方法会被调用）。

注意,即使当两个 Activity 都设置了 singleTask , 比例 A 启动 B, 结果 B 可能会创建新的 task ,也有可能不创建, 不创建则存放到除了 A task 之外已存在的 task ,但是如果 B 没有设置 singleTask ,则 B 会存放到 A task 中.


假设 B 为 singleTask，没有指定 taskAffinity ;B-C-D-E-B,此时会先退出E、D、C ,然后显示 B

- **singleInstance**： 单栈单例单一，创建新的任务栈，将实例放入，一个栈只装一个实例。与 singleTask 不同的是，该新创建的栈不能放别的任何实例。

> 总结：一般 Activity 实例被复用时，都会触发调用 onNewInent() ，也就是不会回调 onCreate() 和 onStart() , 会调用 onResume() ;准确的说，会走 onCreate和 onStart ，但是里面的代码不会重复创建，更像是恢复了状态。

<!-- more -->


# TaskAffinity
其实每个 Activity 任务栈都有一个名字，通常是包名来命名，我们也可以创建一个新的栈，给新的栈命名，但名字不能和包名相同。

而创建新的栈，只需要在清单文件中的 < activity > 标签里面加上 taskAffinity:"栈名"

A: standard
B: singleTask 和 taskAffinity="newtask"
C: singleTask 和 taskAffinity="newtask"

![](http://7xrysc.com1.z0.glb.clouddn.com/activity启动模式.png)


A-B-C-A-B-返回键-返回键 = ？

---
默认栈 有一个实例 A

启动 B：创建 newTask 栈，把 B 放入 newTask 栈

启动 C：把 C 放在 B 的上面

启动 A：因为 A 的启动模式是默认的，在哪个栈启动 A ,A 就应该放在哪个栈，所以 A 放入的应该是 newTask 栈(此时 newTask 栈从上到下有 ACB; 注意-默认栈还有一个最开始的 A 哦）

启动 B： A C 出栈，此时 newTask 栈中只有 B 一个

返回键，B 出栈

返回键，默认栈中的 A 出栈

结果显示： 桌面

---

## Android 5.0+ 
standard模式下，在 5.0 后，跨应用来启动 Activity 出现了点变动。5.0之前：A 应用启动 B 应用的 Activity ,会把 B 的 Activity 放到 A 的栈中。5.0之后：创建一个新的栈来放 B 的 Activity。

还有注意：在5.0之前 使用 startActivityForResult 来获取结果可能为空的问题。

## 标记位
标记类型是在 java 代码的 Intent 中设置的，Intent 代码设置的 flag 会晚于 xml 设置，所以 java 代码的设置会覆盖 xml 的设置。

- FLAG_ACTIVITY_NO_HISTORY
  当该 ActivityA 启动其他 Activity 后，  ActivityA 直接会被移除出栈销毁掉

还有别的几个标记位类型，因为从名字上就能判断出大概的意思，在组合使用时，记得多注意。


- FLAG_ACTIVITY_CLEAR_TOP
清掉 task 中某个实例上面的所有实例，FLAG_ACTIVITY_CLEAR_TOP 通常与 FLAG_ACTIVITY_NEW_TASK 一起使用

- F-LAG_ACTIVITY_NEW_TASK
和 singleTask 一样

- FLAG_ACTIVITY_SINGLE_TOP
和 singleTop 一样


































