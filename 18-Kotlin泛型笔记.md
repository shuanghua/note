---
toc: false
comments: true
description: 对 Kotlin 泛型的加深理解
title: Kotlin 泛型-笔记
tags:
  - Kotlin
id: 20
categories:
  - Kotlin
date: 2017-9-7
---

# Kotlin 泛型

> Kotlin 和 Java 的泛型都是伪泛型。

> Kotlin 泛型有两个概念：**型变**（declaration-site variance）和**类型投影**（type projections）。



<!-- more -->

## 关于 Kotlin 泛型语法定义
- 类 的泛型定义在类名的 后面
```Korlin
class Box<T>(t: T){
    
}

```

- 函数 的泛型定义在函数的 前面
```Kotlin
fun <T> test(){
        
}
```

- 调用 的定义了泛型参数的函数，把需要传入的泛型写在函数名 后面
```Kotlin
test<Int>()
```

- 如果参数的值已经确定了类型，那么可以省略类型
```Kotlin
val box: Box<Int> = Box<Int>(1)
val box = Box(1) //1是 Int 型，因此编译器会推断出我们调用的是 Box<Int>
```

下面进入正题：

# 型变
例：
> 型变：Man 是 Person 的子类，如果 List< Man > 也是List< Person > 的子类。那么就可以称之为类型型变。在 java 中这种特点也称为泛型不可变。
> Kotlin 型变包括了 **协变** 和 **逆变** 以及 **不变** 三种.


## Java 型变
在了解 Kotlin 型变之前，我们先来看看 Java 的泛型型变，在 Java 中我们要用 <? extends T> 指定类型参数的上限，用 <? super T> 指定类型参数的下限。

例：
当 Man 是 Person 的子类，但 List< Man > 和 List< Person > 在 Java 中是一点关系都没有的，不能相互转型！ 如果要让 List< Man > 能赋值给 List< Person > ，我们就得使用 <? extends Person>，看 java Collection 源码的例子：


```Java
public interface Collection<E> extends Iterable<E> {// Java
  boolean add(E e);
  boolean addAll(Collection<? extends E> c);
}
```

add() 和 addAll()，他们的作用分别单个操作和批量操作的区别。再看他们的参数，一个是 E 类型，一个是 <? extends E>, ? extends E 表示只能接收泛型参数类型为 E 子类的任意类型,这样来限定 List<Man> 赋值给 List<Person> 的必备前提条件。

用代码解释：
	list1.add(10)// 这样 E 的类型就变成了 Int 类型。
	list2.addAll(list1)//list1里面的数据类型就得必须是 Int 或 Int 的子类。

## Kotlin 型变

在 Kotlin 就抛弃了这个形式，引入了生产者和消费者的概念

- 生产者是那些只能 读取 数据的对象 ( **out** )(**协变**)

- 消费者是那些只能 写入 数据的对象 ( **in** )(**逆变**)

- 生产啥就消费啥就是**不变**



下面具体看这三个使用方式：

### 协变 out

> 子类类型变成父类类型，比如把 List< Man> 赋值给 List< Person>

Kotlin 中 out 关键字经常遇见，我们来看看 Kotlin 是怎么定义的 Collection

```Kotlin
public interface Collection<out E> : Iterable<E> {// Kotlin
	...
}
```

> out E 表明 E 是协变的，如果两次传给 E 的类型是有关系的，假设 E1 继承 E2，那么 Collection < E1 > 和 Collection< E2 > 也是有关系的。


这就是 Kotlin 实现 Java 泛型的相同之处。但这是 Kotlin ,怎么会是换一个定义这么简单，还记得上面我们有提到过生产者和消费者吗，这才是 kotlin 的不同之处。

在 Kotlin 中，out 被赋予了生产者的概念：被 out 修饰的类型只能用于返回值类型。

- 对比两段我自写的代码例子就很清晰了：
```Kotlin
public interface MyList<out T> {// Kotlin
	public fun add(t: T) // 编译器报错
	public fun addAll(t: T) // 编译器报错
}
public interface MyList<out T> {// Kotlin
	public fun add(t: T) // 编译器报错
	public fun addAll(): T // 正确
}
```
被 out 修饰的 T ,只能做为返回值类型，不能作为函数参数类型，因为函数的参数是用来消费。


#### @UnsafeVariance
上面说 out T, 说明这个 T 只能用于生产(只能用于返回值类型)，但有些特殊情况下，我们既需要这个 T 用于返回值类型，又需要用于函数参数类型, 那么就需要用到 @UnsafeVariance 注解，告诉编译器确定此处安全，不用编译器进行安全检测，从而达到两处使用的目的。

在 Kotlin Collection 接口里的一个函数例子：

```Kotlin
public interface Collection<out E> : Iterable<E> { 
     public operator fun contains(element: @UnsafeVariance E): Boolean 
} 
```

本来这个 E 已经被修饰为: out 协变, 本不可在 contains（） 函数的参数中使用，但借助了 @UnsafeVariance 注解，就可以被用做函数的参数类型。使用 @UnsafeVariance 的前提是确保类型安全即可。


#### 什么是类型安全？
> Kotlin 是一门静态编程语言，在编译期就自动做类型安全检查。比如把一个 Double 类型赋值给 Int 类型，会造成精度丢失，说明这是不安全的类型赋值操作。

下面的代码很好的解释了这个情况：

```Kotlin
class MyCollection<out T>{ 
    fun add(t: T){//报错
    } 
}
```

调用
```Kotlin
var list: MyCollection<Number> = MyCollection<Int>() //在声明的时候确定了 T = Int 类型
list.add(3.0)// E 的类型设置为 Double
```
- Double 赋值给 Int 的操作，丢失精度，不是安全赋值。
- 调用处的代码可以编译通过的，而 MyCollection 中的报错是必然的，因为 out 的限定。
- 当使用 @UnsafeVariance 注解后，编译器就不提示冲突了，这样我们后面调用 add 的时候可以传入 Int Double 就没问题了。
```Kotlin
class MyCollection<out T>{ 
    fun add(t: @UnsafeVariance T){//不报错
    } 
}
```

> 但这样新的问题就来了：add 是一个集合操作，里面有 Int，又有 Double, 我要遍历怎么遍历？这就造成了读取的问题。这就尴尬了，所以在使用 @UnsafeVariance 注解的时候，尽量确保第二次给 T 确定类型的时候也是同样的类型。这也是静态语言的特点，为了解决类型的不确定性，就有了下面的 **逆变** 概念。


## 逆变 in
> 父类类型变成子类类型,比如把 Comparable< Number> 赋值给 Comparable< Int>

```kotlin
interface Comparable<in T> {
    operator fun compareTo(other: T): Int
}

fun demo(f: Comparable<Number>) {
    f.compareTo(1.0)
    val a: Comparable<Double> = f // OK!
}
```

上面的  @UnsafeVariance 注解使用的前提是你得确保你的类型安全。如果不能确定，那怎么办？So 就有了这个**逆变**的概念

> in T, 就是这个 T 只能用于参数类型

- 理解了 out ,in 的使用就很简单了，所以这里就不详细介绍了。
```Kotlin
public interface MyList<in E, out T> {// Kotlin
	public fun addAll(e: E): T // 正确
}
```


## 不变
没有关系的就是不变的


# 声明处型变
声明处型变也可以称为使用处型变

```Kotlin
interface Source<out T> {
    fun nextT(): T
}
```

```Kotlin
fun test(str: Source<String>) {
    val obj: Source<Any> = str//在此处声明了一个 Any 类型，这就和 Java 一样能在使用处发生型变
}
```


# 类型投影（*）

当你对参数类型一无所知的时候，又想安全的使用这个泛型，保险的方法就是定一个该泛型的投影，每个该泛型的正确实例都将是该投影的子类。

Kotlin 可以根据 * 所指代的泛型参数进行相应的映射，看官方例子：

- 对于 Foo < out T>，其中 T 是一个具有上界的**协变**类型参数，Foo <**> 等价于 Foo < out TUpper>。 这意味着当 T 未知时，你可以安全地从 Foo <*> 读取 TUpper 的值。

> Foo<*> = Foo< out T >

- 对于 Foo <in T>，其中 T 是一个**逆变**类型参数，Foo<**> 等价于 Foo< in Nothing>。 这意味着当 T 未知时，不能以任何方式安全的方式写入 Foo < *>。

> Foo<*> = Foo< in T >

- 对于 Foo <T>，其中 T 是一个具有上界 TUpper 的**不型变**类型参数，Foo<*> 对于读取值时等价于 Foo<out TUpper> 而对于写值时等价于 Foo< in Nothing>。

> Foo<*> = Foo< T >

# Kotlin 泛型约束

在 Kotlin 用冒号 : 指定上界：

```Kotlin
fun <T : Comparable<T>> sort(list: List<T>) {
	// ...
}
```

这样就只有 Comparable<T> 的子类才能传给泛型 T, 也就是 List 里的类型只能是 Comparable<T> 的子类型。

```Kotlin
sort(listOf(1, 2, 3)) // Success
sort(listOf(HashMap<Int, String>()))//Error HashMap 不是 Comparable<T> 的子类型
```

在尖括号内只能定义一个上界，如果要指定多种上界，需要用 where 语句指定：

```Kotlin
fun <T> cloneWhenGreater(list: List<T>, threshold: T): List<T>
    where T : Comparable,
          T : Cloneable {
  return list.filter { it > threshold }.map { it.clone() }
}
```


# 笔记

> out T 等价于 ? extends T
> in T 等价于 ? super T
> \* 等价于 ?

> out T, 说明这个 T 只能用于生产(通常只能用于返回值类型)
> in T, 就是这个 T 只能用于参数类型
> 对应一些默认不支持型变的类，可以通过复制来型变它。像 Array<T>