---
toc: true
comments: true
description: Kotlin 关键字-笔记
title: Kotlin 关键字-笔记
tags:
  - Kotlin
id: 13
categories:
  - Kotlin
date: 2017-8-17
---

# val 只读属性


# var 可变属性


# const 编译时常量
- 编译时常量，需要满足以 String 或基本类型进行初始化
- 没有自定义 getter 对象成员或顶级（Top#level）


# constructor 构造函数
- 一个类可以有一个主构造函数以及多个二级构造函数
- 如果主构造函数没有 注解 或者 可见性 说明，constructor 关键字是可以省略


<!-- more -->

# crossinline 保持内联有效
- crossinline 就是在你的 lambda 没有直接在上下文中调用 ,而是被传到了其他地方作为嵌套在内部的代码，这样内联会失效，所以需要 crossinline


# companion object 静态方法块
-  companion object {  } 大括号中的方法和变量为静态


# companion 伴生对象
- 伴生对象，可以使用类名作为修饰符调用伴生对象的成员。伴生对象的名字可以被省略，因为在调用伴生对象成员时不涉及伴生对象的名字，这种情况下在使用伴生对象时用 Companion 替代伴生对象名

- 伴生对象看起来像 Java 的静态成员，但是在运行时他们仍然是对象的实例成员，并且可以实现接口


# data 数据类
- 编译器会自动添加 equals / hashCode / toString() / copy() 方法
- 主构造函数应该至少有一个参数
- 主构造函数的所有参数必须标注为 val 或者 var
- 数据类不能是 abstract / open / sealed / inner
- 不能继承其他类（可以实现接口）
- JVM中如果构造函数是无参的，则所有属性必须有默认的值


# enum 枚举
- 枚举类，同 Java​


# field 幕后字段


# internal 模块内可见
- 在 本模块 的所有可以访问到声明区域的均可以访问该类的所有 internal 成员
​

# inner 非静态内部类
- 非静态内部类默认持有外部类的状态
- 类可以标记为 inner 这样就可以访问外部类的成员，拥有外部类的一个对象引用


# infix 中缀表达式
- 可以让在调用该函数时，不用写点号和括号


# inline 内联函数


# noinline 禁用内联


# lateinit 延迟初始化属性
- 只能被用在类的 var 可变属性定义中，
- 不能用在构造方法中，属性不能有自定义 setter / getter，
- 属性类型不能为空，也不能为基本类型
​

# operator 运算符重载​


# open 开放权限
- 允许别的类继承这个类
- 默认情况下，kotlin 中所有的类都是 final 的
​

# override 复写
- 复写父类的函数，如果不加则非法


# object 
- Kotlin 用对象表达式和对象声明实现 Java 匿名内部类这种情况，即匿名类的实现对象为 object

```Kotlin
object:OnClickListener{

}
```

- 通过这种方式，我们可以很轻松地实现单例 [Kotlin 单例](https://moshuanghua.com/2017/09/22/Kotlin%20%E5%8D%95%E4%BE%8B/)
- 如果 object 关键字后面有名字，它不再是一个表达式。不能将它赋值给变量，但是我们可以通过他的名字引用它。这样的对象可以拥有超类


# sealed 密封类
- 类名关键字，密封类，值只能是有限集合中的某种类型，相当于是一个枚举类的扩展。
- 密封类可以有子类但必须全部嵌套在“ 同一个KT文件中 ”声明


# suspend 协程
详见：[Kotlin 协程](https://moshuanghua.com/2017/08/27/Kotlin%20%E5%8D%8F%E7%A8%8B-%E7%AC%94%E8%AE%B0/)


# suspend 与 supspend 使用
详见：[Kotlin 协程](https://moshuanghua.com/2017/08/27/Kotlin%20%E5%8D%8F%E7%A8%8B-%E7%AC%94%E8%AE%B0/)

# tailrec 尾递归优化
- 尾递归：返回时调用自己且不做其他的操作


# interface 接口
接口的方法可以有自己的实现，可以有属性但必须是抽象的、或提供访问器的实现


# @ 引用
- var a = this@A
- 表示 a 为 A 的引用（内部类中明确引用）
- Android 中 this@MainActivity 相当与 MainActivity.this

# as 类型转换
- y as String (将 y 转换为 String 类型)


# is 即 instance，类型检查
- if(a is Int)

# out 协变

- 使用 out 符号进行修饰，如 <out T>，表示类型为 T 或 T 的子类
```
val listInt: Array<Int> = arrayOf(1, 2, 3)
var from: Array<Any> = listInt //报错
var from: Array<out Any> = listInt //正确
```

详见：[Kotlin 泛型](https://moshuanghua.com/2017/09/07/Kotlin-%E6%B3%9B%E5%9E%8B%E7%AC%94%E8%AE%B0/)


# in 逆变
  - for(i in List)

详见：[Kotlin 泛型](https://moshuanghua.com/2017/09/07/Kotlin-%E6%B3%9B%E5%9E%8B%E7%AC%94%E8%AE%B0/)

# ?. 安全调用
- b?.length，b 为空时返回 null，否则返回 length


# !.
- b!.length，要求 b 不能为空，否则编译错误


# !!.
- b!!.length，返回一个非空对象 (Int?) 或者抛出一个 NPE


# ?：
- var l = b.length() ?: 0
- 左边表达式不为空时返回表达式的结果，否则返回右边的值


# by 代理
```Kotlin
by lazy {
}
```
- 属性代理，自定义一个自己的 lazy 取名 X，则需要定义一个 X 类，
- 然后写一个 operator fun getValue 或 setValue 方法(取决于变量的可读写)

# $ 字符串模板
- 取代 Java 中的 + 拼接（Java 中用 + 号来拼接字符串需要写一大堆双引号）