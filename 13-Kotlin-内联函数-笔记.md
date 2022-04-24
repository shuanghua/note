---
title: Kotlin 内联函数-笔记
tags:
  - Kotlin
id: 15
categories:
  - Kotlin
toc: true
comments: true
description: 程序都是一步一步往下执行，遇到函数时（非内联函数）就查找跳到到函数所在的地址位置进行编译，完成后再回到调用的地方，如果这个函数在别的类、包中或者各个函数来回调用，那这段编译执行将是非常耗时的。
date: 2017-8-22
---

# 内联函数 inline

- 在高阶函数中，每一个函数都是一个对象
- 内存分配和虚拟调用会降低程序的运行效率
- 印象中好像 C++ 有内联函数的概念，ㄟ( ▔, ▔ )ㄏ

> 内联函数的 Lambda 表达式可以解决这样的问题。

> 定义为内联的函数，在编译时放到调用处进行编译运行, 而不是函数所在的地方编译运行


<!-- more -->

# 文字例子

- 程序都是一步一步往下执行，遇到函数时（非内联函数）就查找跳到到函数所在的地址位置进行编译，完成后再回到调用的地方，如果这个函数在别的类、包中或者各个函数来回调用，那这段编译执行将是非常耗时的。

# 代码例子

## 普通函数

```Kotlin
fun main(args: Array<String>) {
    println("出门上班")
    work()
    println("下班回家")
}

fun work(){// 公司很远呢
    println("开始工作啦")
}
```
### 普通函数字节码反编译成Java后的代码

![](http://7xrysc.com1.z0.glb.clouddn.com/%E9%9D%9E%E5%86%85%E8%81%94.png)


## 内联函数
```Kotlin
fun main(args: Array<String>) {
  println("出门上班")
  work()
  println("下班回家")
}

inline fun work(){// 公司很近呢
    println("开始工作啦")
}
```
### 内联函数 字节码反编译成 Java 后的代码
![](http://7xrysc.com1.z0.glb.clouddn.com/%E5%86%85%E8%81%94.png)


---
- 可以看到内联函数，把代码复制一份放到 main 函数里编译，这样的执行效率是不是会快很多。
- 但缺点也很明显，在 work 函数中也有同样的代码，这样就会导致生成的代码过多。
- 所以当函数的代码很多的情况下，尽量不要使用内联函数。

# 附加
- 默认情况下，inline 修饰的函数（内联函数）的 Lambda 表达式参数也是内联的，就是说参数也是放到调用的地方去
- 非默认情况下，内联函数的 Lambda 表达式参数前加 noinline 关键字修饰 （禁用内联）

```Kotlin
fun main(args: Array<String>) {
    println("Start")
    println("End")
}

inline fun work(s: String, noinline e: () -> Unit) {
    println("开始工作啦")
}
```

- noinline 只能放在 e 前面，不能放在 s 前面， 因为 s 的类型不是 Lambda 表达式


# 非局部返回
- 所谓非局部返回就是： 内联函数 + Lambda 表达式 + return 的语法使用
- 默认情况下 Lambda 里不能使用 return，但 Lambda 是内联的就可以使用 return
```Kotlin
fun hasZeros(ints: List<Int>): Boolean {
  ints.forEach {
  if (it == 0) return true // 从 hasZeros 返回, forEach 是内联的，所以可以使用 return
  }
  return false
}
```

# crossinline
- crossinline 就是在你的 lambda 没有直接在上下文中调用 ,而是被传到了其他地方作为嵌套在内部的代码，这样内联会失效，为了防止这种情况下的内联失效，所以就需要到 crossinline 关键字。

## 例如
```Kotlin
inline fun f( body: () -> Unit) {
    val f = object: Runnable {
        override fun run() = body() // 这里的 body() 会报错
    }
}
```
- 内联函数的参数是 Lambda 表达式，则这个参数也是内联的
- body 没有在当前函数中被使用，而是传到了 Runnable 里面去使用，这样有可能会导致 body 内联失效
需要在参数 body 前用 crossinline 修饰才能内联

```Kotlin
inline fun f(crossinline body: () -> Unit) {
    val f = object: Runnable {
        override fun run() = body() //正确
    }
}
```
