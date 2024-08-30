---
toc: true
comments: true
description: 一个类可以有一个主构造函数和多个次构造函数
title: Kotlin 构造函数-笔记
tags:
  - Kotlin
id: 12
categories:
  - Kotlin
date: 2017-5-29
---

# 构造函数
一个类可以有一个主构造函数和多个次构造函数

## 主构造函数
- 声明在类名后面,用 constructor 关键字修饰
```Kotlin
class Person 注解 或 可见性修饰符 constructor(主构造函数参数){
    //有 注解 或 可见性修饰符
}
```


<!-- more -->

- 没有注解或可见性修饰符时，constructor 可去掉不写，有则必须写
```Kotlin
class Person(主构造函数参数){
    //没有 注解 或 可见性修饰符
}
```

- 主构造函数的初始化，在 Kotlin 中 “主” 构造函数不能包含任何代码，所以初始化的代码可以放到 init 关键字为前缀的代码块中
  ```Kotlin
  class Person(构造函数参数){
    init{
    //初始化工作
    }
  }
  ```

- 主构造函数的参数能在该类的任何地方使用，在该类的“方法中”使用时需要给参数加上 var 或 val，但不能在该类的内部类中使用

## 次构造函数（次构造函数参数不能有 var 或 val 声明）
- 次构造函数不能有声明 val 或 var
-  如果类有一个主构造函数（无论有无参数），每个次构造函数需要直接或间接委托给主构造函数，用this关键字


### 没有主构造函数时
```Kotlin
class Person{
  //一个参数的次构造函数
  constructor(name1: String) {//不用委托
  }

  //两个个参数的次构造函数
  constructor(name2: String, age: Int) : this(name2) {//委托
  }
}
```

##### 有主构造函数时
```Kotlin
class Person(name : String){
  constructor(name1:String,age1:Int):this(name1){//委托
  }

  constructor(name2:String ,age2:Int,id:String):this(name2.age2){
  }
}
```
