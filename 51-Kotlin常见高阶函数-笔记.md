---
toc: false
comments: false
title: Kotlin StandardKt 非标准函数使用总结
tags:
  - Kotlin
id: 51
categories:
  - Kotlin
date: 2019-2-6
---

#### Kotlin StandardKt 非标准函数使用总结

Kotlin let apply run with 系列总结

```kotlin
//=====================以下类似 map 返回转换后的结果==================

// 需要修改调用者，并返回另一个对象时 （要求 Lambda 最后一行是可返回的）
inline fun <T, R> T.let(block: (T) -> R): R {
    return block(this)
}

// 显式传入实例，然后扩展该实例，返回另一个实例
inline fun <T, R> with(receiver: T, block: T.() -> R): R {
    return receiver.block()
}

// 修改调用者里面的方法或对象，返回非调用者对象 （要求 Lambda 最后一行是可返回的）
inline fun <T, R> T.run(block: T.() -> R): R {
    return block()
}

// 传入一个函数，返回函数的结果（就是方便使用 lambda 函数）
inline fun <R> run(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}


//=========================范围======================

// 需要直接对调用者进行修改或者需要在代码快中使用调用者，并返回修改后的调用者
inline fun <T> T.also(block: (T) -> Unit): T {
    block(this)
    return this
}

// 需要对修改调用者中的属性，然后返回调用者
inline fun <T> T.apply(block: T.() -> Unit): T {
    block()
    return this
}



//=========================判断======================

// 用于判断布尔类型，只有为 true 才继续向下调用，然后返回调用者，否则返回 null
inline fun <T> T.takeIf(predicate: (T) -> Boolean): T? {
    contract {
        callsInPlace(predicate, InvocationKind.EXACTLY_ONCE)
    }
    return if (predicate(this)) this else null
}

// 和 takeIf 的返回值相反，只有为 false 才继续向下调用，然后返回调用者，否则返回 null
inline fun <T> T.takeUnless(predicate: (T) -> Boolean): T? {
    contract {
        callsInPlace(predicate, InvocationKind.EXACTLY_ONCE)
    }
    return if (!predicate(this)) this else null
}

// 让某一个函数执行多少次
inline fun repeat(times: Int, action: (Int) -> Unit) {
    contract { callsInPlace(action) }

    for (index in 0 until times) {
        action(index)
    }
}
```

#### 总结

apply 和 also 都是返回调用者，如果你只需要在 lambda 中使用调用者里面的属性，请使用前者； 如果你需要在 lambda 中使用调用者，那么请使用后者。

上面的 this 一般指的就是调用者，如果把 this 传入给 lambda ，那么我们就可以在 lambda 中使用调用者； 如果是 lambda 扩展了 this （扩展了 T） ，那么我们在 lambda 中只能使用调用者里面的属性和函数。两句话来总结：

1. lambda 里面能拿到调用者还是只能拿到调用者的属性。
2. 返回值是返回调用者还是返回 lambda 的结果。

#### 后续

```kotlin
var a = 
a?.let { //... }
if (a != null) { //... }
```

- if 和 let 有什么区别？
  
> 答：如果 a 是 var, 那么在 let 大括号里面时，即使外面 a 的值被修改了，let 里面依然是安全的。因为 let 中使用的是闭包，而 if 就不一定了。

- 什么时候用 if? 什么时候用 ?.
  
> 先考虑上面的情况，再配合下面的条件：
当同时需要判断为 null 和不为 null 时，使用 if 。
> 当大括号内只有一行代码且被操作的属性是 val 时，使用 if

```kotlin
// 第一都是从内存读取 
 weather?.let { it -> 
        safeBinding.weather = it
        safeBinding.weather = it
        safeBinding.weather = it
    }

    if (weather != null) {//第一都是从内存读取
        safeBinding.weather = weather
    }

    // 当只有一行代码且 weather 是 val ，使用 if 更好。
}
```

> 当大括号内只有一行代码或多行代码且被操作的属性是 var 时，可以优先考虑使用 let

如果需要判断满足不为 null 且又要满足某个条件时，使用 if

```kotlin
if (!list.isNullOrEmpty()) {
    list[0].cityId
} else {
     ""
}
```

当然如果你的表达式非常的简单也可以使用 elvis 表达式，像下面这样：

```kotlin
val size = list?.size ?: 0
```
