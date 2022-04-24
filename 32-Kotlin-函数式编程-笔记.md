---
toc: false
comments: true
title: Kotlin 函数式编程练习
description: 为了能更了解和使用函数思想去编写代码,所以写了如下一些关于 Kotlin 函数式编程的笔记
tags:
  - Kotlin
id: 32
categories:
  - Kotlin
date: 2018-3-28
---

最近都在使用 Kotlin 写 Android, 由于受到以前 Java 的编写思想影响,写出来的代码都是 Java 式的 Kotlin 代码 , 缺少 Kotlin 函数式编程的特色,为了能更了解和使用函数思想去编写代码,所以写了如下一些关于 Kotlin 函数式编程的笔记.


#### 题1：过滤掉一个集合中长度为 偶数 的内容

首先定义一个集合；因为是过滤，所以 filter 函数少不了，filter 接收参数一个  Lambda ，意思是如何让我们自己去实现过滤的条件。所以下面主要针对这个 Lambda 去展开工作。

- 定义集合

```kotlin
fun main(args: Array<String>) {
    val strings = listOf("a", "ab", "abc")
	string.filter{  }
}

```

从上面代码中看到，我们可以直接在大括号中写具体的过滤代码，但是这样不够优雅；我们知道在 Kotlin 中函数是一等公民，可以把函数当作参数使用。因此我们选择第二种方式 string.filter()，把大括号换成括号。

- 分析过滤的条件

1.内容的长度
2.将这个长度对 2 求余，判断是否等于 0

将两个条件写成代码：isOdd(length(str))
```kontlin
fun length(s: String) = s.length
fun isOdd(x: Int) = x % 2 != 0
```

然后我们又发现 filter() 只能接受一个参数，所以我们得需要使用到 复合函数，将这两个函数复合成一个函数，这样就能传给 filter() 了。

- 函数复合
关于复合函数的使用，请看：[复合函数](https://moshuanghua.com/2017/08/21/12-Kotlin%20%E5%87%BD%E6%95%B0%E5%A4%8D%E5%90%88-%E7%AC%94%E8%AE%B0/)

filter() 里面的 Lambda 函数的返回值是一个 Boolean 类型，所以我们定义的复合函数的返回值类型相应的也是 Boolean, 下面我们直接把 compose 函数的返回值定义为  (String) -> Boolean 类型，这样最终返回的是 Boolean ,同时也能使用 String 类型.

```kotlin
fun compose(f: (String) -> Int, g: (Int) -> Boolean): (String) -> Boolean {
    return { x -> g(f(x)) }
}
```

为了通用,利用泛型将上面的代码封装如下:
```kotlin
fun <A, B, C> compose(f: (A) -> B, g: (B) -> C): (A) -> C {
    return { x -> f(g(x)) }
}
```

- 最后的调用

```kotlin
val oddLength = compose3(::length, ::isOdd)//length为包级函数,所以所以可以用 ::
val strings = listOf("a", "ab", "abc")
println(strings.filter(oddLength)) // [a, abc]
```