---
title: Rxjava2 笔记
tags:
  - Android
id: 11
categories:
  - Android

toc: true
comments: true
description: 当发送过来的数据速度快于处理数据的速度时，就需要用到 Backpressure

date: 2017-2-25
---

# Backpressure (背压) 概念理解：
当发送过来的数据速度快于处理数据的速度时，就需要用到 Backpressure ，Backpressure 是响应流控制的其中一个方案。
这里引用了 大神 Jake Warton 的一个例子，[https://realm.io/cn/news/gotocph-jake-wharton-exploring-rxjava2-android/](https://realm.io/cn/news/gotocph-jake-wharton-exploring-rxjava2-android/ "Jake Wharton -rxjava2-android")

![](http://7xrysc.com1.z0.glb.clouddn.com/jake%20wharton.png)


<!-- more -->

# 数据类型
1. Observable (不支持 Backpressure)
2. Flowable  （支持 Backpressure）

## Obervable
Single 是 Observable 三个子集中的其中一个子集:只返回一个结果，要么是正确的结果数据，要么就是抛出异常，没有别的。与 scaler 方法类似，只不过　Single 是响应式，scaler 是命令式。

## Completable
错误时抛出异常，成功时没有任何返回。

## Maybe
同 Single，再加上一个可能，结果可能返回是空值

# Flowable 
在 Flwable 下的操作符强制支持背压

# Rxjava2 实现标准
Rxjava2 遵循了 Reactive Streams Specification (响应流)规范，主要体现在四个基础接口
1 Publisher 
2 Subscriber
3 Subscription
4 Processor
