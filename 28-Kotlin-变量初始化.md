---
toc: false
comments: true
title: Kotlin 变量初始化的三种方式
description: lateinit、by lazy
tags:
  - Kotlin
id: 30
categories:
  - Kotlin
date: 2018-1-14
---

### Kotlin 三种变量初始化比较：


1. 直接初始化,一般用于基本数据类型，占用内存较小的变量,可用于 val 或 var 。

```kotlin
var str: String = "Hello"
```

2. lateinit 延迟初始化, 不适用于基本数据类型，一般用于资源访问对象的初始化和引用类型变量的初始化，仅可用于 var 。

```kotlin
lateinit var drawable: Drawable

lateinit var person: Person
```

3. by lazy 懒惰初始化/懒加载/用时初始化，其只有在第一次访问使用该变量时，该变量才会初始化；该变量的值仅在一个线程中计算，并且所有的线程都只会看到相同的值；当第二次访问时返回的是第一次的值；懒惰初始化一般用于占用内存较大或计算量大的变量，例如，Bitmap、访问磁盘文件等。仅可用于 val 。

```kotlin
val myBitmap by lazy{
	//初始化 myBitmap
}
```
