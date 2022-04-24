---
title: Kotlin 函数复合-笔记
tags:
  - Kotlin
id: 14
categories:
  - Kotlin
toc: true
comments: true
description: 函数复合
date: 2017-8-21
---


# 函数复合
假设 有两个函数：
- 传入一个 Int 参数，对这个参数 + 5 ,最终返回结果 fun1 = { i : Int -> i + 5 }
- 传入一个 Int 参数，对这个参数 * 2 ,最终返回结果 fun2 = { i : Int -> i * 2 }

需求： 随便传一个值给 fun1 ，然后将 fun1 的结果 传给 fun2 ，计算最终的结果

---



<!-- more -->

# 实现
## 定义函数复合格式
- 利用中缀表达式
- 扩展 Function 系列函数，自定义新的扩展函数名（ 这里命名为：fuHe ）

``` Kotlin

/**
 * A : 用户传入的参数泛型
 * P : 两个函数复合的中间变量(桥梁)
 * R : 返回值参数泛型
 */
infix fun <A, P, R> Function1<A, P>.fuHe(function: Function1<P, R>): Function1<A, R> {
  return fun(a: A): R {
      return function.invoke(this.invoke(a))
  }
}

```

这里 this 相当于 fun1, function 相当于 fun2

谁调用了 fuHe , this 就指代谁，在 Kotlin 中 this 通常指的是他的调用者。

## 调用
```Kotlin
fun main(args: Array<String>) {

    val fun1 = { i : Int -> i + 5 }
    val fun2 = { i : Int -> i * 2 }

    val result_12 = fun1 fuHe fun2 //复合后的函数：result_12
    println(result_12( 2 ))  //调用复合后的函数，结果是 14

    //置换复合顺序
    //val result_21 = fun2 fuHe fun1 //复合后的函数：result_21 //和文章最后的 附加的等价
    //println(result_21( 2 ))  //调用复合后的函数，结果是 9
}

```
现在我们来分析一下上面的代码，先从需求开始
- 需求是 结果 = fun2( fun1( A ) ) 这个应该能看懂吧。A 指用户传入的参数，假设 A = 2，那么 结果 = fun2( fun1( 2 ) ) = 14

- 分析 val result_12 = fun1 fuHe fun2

  1 . 从我们定义的复合函数上面的注释可得知：this = fun1(), function  = fun2()

  2 . 那么用 this 和 function 来表达我们的需求就是：

  3 . function.invoke(this.invoke( A ))  等价于 fun2(fun1( A ))

- 根据 function.invoke(this.invoke( A ))  来定义复合格式

  1 .  infix fun< A, P, R >固定泛型参数可以随意取名

  2 .  第一个 Function1 指的就是我们的 this ，其格式是 this（A）,所以 Function1< , > 的第一个泛型参数就是 A，第二是返回的泛型参数（准确点是最后一个参数是返回的参数）Function1< A , P >

  3 . 第二个 Function1 指的就是我们的 function ，其格式是 function(this( A )),所以其 Function1< , > 第一个泛型参数是第一个 Funtion1< , > 的第二个泛型参数（返回的泛型参数） Function1< P , R >, 最终的 R ，因为是运算玩第二函数就已经得到结果了，所以直接返回所得的结果。

  4 . 第三个 Function1 就是整个该函数的接收参数和返回值的泛型参数，接收一个用过自定义的数，然后将计算的结果返回(匿名函数)

---


# 附加
- 在不改变  val result_12 = fun1 fuHe fun2 复合顺序的情况下，写一个 fun1(fun2( A ))
- 分析得：this.invoke(function.invoke(A))
- 拆分：
  1 . this.invoke() < ? , R >  // 最后运算 this,所以 R 一定在 this 中
  2 . function.invoke(A) < A , ? > // function 需要参数 A, 所以  A 在 function 中
  3 . 因为两个函数要复合，所以两个 ? 两个泛型参数一定是相同的，所以 ? = P


  - 代码
  ```Kotlin
  infix fun <A, P, R> Function1<P, R>.compose(function: Function1<A, P>): Function1<A, R> {
      return fun(a: A): R {
          return this.invoke(function.invoke(a))
      }
  }
  ```
