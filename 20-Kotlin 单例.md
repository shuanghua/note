---
toc: true
comments: true
description: Java 单例和 kotlin 单例的 5 种写法对比
title: Kotlin 单例
tags:
  - Kotlin
id: 22
categories:
  - Kotlin
date: 2017-9-22
---

# 常见的 Java 单例和 kotlin 单例的 5 种写法对比

> 示例图片地址失效了，所以不能显示

线程安全： 类加载时线程是安全的；上锁时线程是安全的。
懒加载： 在调用时加载。


<!-- more -->


## 1.类加载时创建( 懒人写法 )

- 线程安全，但拖慢类的创建启动时间

![](http://7xrysc.com1.z0.glb.clouddn.com/KotlinSingle1.png)


## 2.需要时创建（ 基本懒加载 ）

- 不是线程安全，不拖慢类的创建启动时间

![](http://7xrysc.com1.z0.glb.clouddn.com/KotlinSingle2.png)


## 3. 单锁懒加载

- 线程安全，每次调用都上锁，消耗性能

![](http://7xrysc.com1.z0.glb.clouddn.com/KotlinSingle3.png)


## 4. 双重检查

- 线程安全，只有需要上一次锁就足够,Java 中写法繁杂。

![](http://7xrysc.com1.z0.glb.clouddn.com/KotlinSingle4.png)


## 5. 静态内部类

- 线程安全，同时也是懒加载，写法简单。

![](http://7xrysc.com1.z0.glb.clouddn.com/KotlinSingle5.png)

# 6.枚举
> 枚举其实也是一单例的一种。写法就是普通写法，java 和 kotlin 也都是一样的写法。

- 线程安全、懒加载、可序列化和反序列化，简单粗暴
- 在 Android 开发中，其实对于枚举使用是存在争议的，主要问题是内存开销问题。
- 目前根据 Android 官方文档提示：并不建议在 Android 开发中使用过多的枚举

下面是摘自 Android 官方有关性能优化文档中的一句话：
> [Enums often require more than twice as much memory as static constants. You should strictly avoid using enums on Android](https://developer.android.com/topic/performance/memory.html#Overhead)（通常枚举需要的内存的是静态常量内存的两倍多。 您应该严格避免在 Android 上使用枚举。）


- 在 Android 虚拟机日渐成熟的今天，我个人认为枚举是可以使用的，但避免过多的使用，在喜欢追求代码易读性和美观性推荐使用枚举，注重应用内存性能，像嵌入式开发啥的就少用枚举。