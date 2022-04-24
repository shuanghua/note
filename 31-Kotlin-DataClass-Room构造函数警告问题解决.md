---
toc: false
comments: true
title: Kotlin DataClass 和 Room 构造函数警告问题解决
description: Kotlin DataClass 和 Room 构造函数警告问题解决
tags:
  - Android
id: 31
categories:
  - Android
date: 2018-2-22
---

Android Room 数据库在 Kapt 生成 Java 代码时会默认选择无参的构造函数。如果 Entity 类中存在多个构造函数，Room 在编译时会报出警告：
警告: There are multiple good constructors and Room will pick the no-arg constructor. You can use the @Ignore annotation to eliminate unwanted constructors.

![](http://7xrysc.com1.z0.glb.clouddn.com/RoomConstructorsWarnming.png)	

下面利用 Room 的 @Ignore 注解配合二级构造函数来消除该警告：

```kotlin
@Entity(tableName = "person")
data class Person @Ignore constructor(var name:Stirng,
		var age:Stirng,
		var id:String){
	constructor(): this("","","")
}
```