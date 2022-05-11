---
toc: false
comments: false
title: Kotlin Android 序列化和反序列化的折腾
description: Kotlin Android 序列化和反序列化的折腾
tags:
  - Kotlin
id: 57
categories:
  - Kotlin
date: 2019-5-1
---

Kotlin Android 序列化和反序列化的折腾 ,主要针对  MoShi 和 Kotlin 官方的 KS
<!-- more -->


# 数据模型

```kotlin
data class PersonWrapper(
    var name: String? = null,
    val hand: Person
) 

open class Person 

data class Tom(
    var name: String? = null,
    var age: String? = null
) : Person()

data class Jake(
	var name: String? = null,
	var phone: String? = null
) : Person()
```

上面是我们常见的三个数据模型，Person 被 Tom 和 Jake 继承着,所以 Person 具有多态的特性

当我们对 PersonWrapper 这个类做序列化的时候，非常的简单，new 出相关实例，然后使用相关序列化库序列化即可。因为是针对 Kotlin 代码的序列化，下面是我只用 MoShi 和 KS 序列化，主要是这两个对 Kotlin 支持更好。

#### MoShi

```groovy
implementation 'com.squareup.moshi:moshi:1.8.0'
implementation 'com.squareup.moshi:moshi-adapters:1.8.0'
kapt 'com.squareup.moshi:moshi-kotlin-codegen:1.8.0' //这里用的是 Kapt ，不是 反射
```

```kotlin
data class PersonWrapper(
    var name: String? = null,
    val hand: Person
) 

open class Person 

data class Tom(
    var name: String? = null,
    var age: String? = null
) : Person()

data class Jake(
	var name: String? = null,
	var phone: String? = null
) : Person()

private fun testMoShi() {
	val moshiAdapter: JsonAdapter<PersonWrapper> = Moshi.Builder()
		.add(
			PolymorphicJsonAdapterFactory
				.of(Person::class.java, "type")
				.withSubtype(Person.Tom::class.java, "blackjack")
				.withSubtype(Person.Jake::class.java, "holdem")
		).build().adapter(PersonWrapper::class.java)

	val tom = Person.Tom("tom", "20")
	val wrapper = PersonWrapper("白", tom)
        
	val wrapperString = moshiAdapter.toJson(wrapper)//保存本地专用
	val wrapperObj = moshiAdapter.fromJson(wrapperString)

	println("wrapperString=$wrapperString}") 
   // {"color":"白","person":{"type":"tom","age":"20","name":"tom"}}}
	println("wrapperObj=$wrapperObj}")
}
```

在多态的环境下，序列化非常容易，但是反序列化的时候就需要考虑多态的性质，编译器可不知道你需要把 string  具体转换成哪个类，而一般该问题的解决办法通常都是在 序列化的时候添加多余的字段标记 , 再在反序列化的时候根据对应的标记进行还原转换。

在碰到多态情况下，记得添加 moshi-adapter 这个库。



## KS

```groovy
implementation "org.jetbrains.kotlin:kotlin-reflect:$kotlin_version" //kotlin 反射库
implementation "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.1"
```



```kotlin
@Serializable
data class PersonWrapper(
    var color: String? = null,
    @Polymorphic
    val person: Person
)

sealed class Person {

    @Serializable
    @SerialName("Tom")
    data class Tom(
        var name: String? = null,
        var age: String? = null
    ) : Person()

    @Serializable
    @SerialName("Jake")
    data class Jake(
        var name: String? = null,
        var age: String? = null,
        var phone: String? = null
    ) : Person()
}

private fun testKS() = lifecycleScope.launch(Default) {
    val json = Json(
        JsonConfiguration.Stable.copy(classDiscriminator = "type"),
        context = SerializersModule {
            polymorphic<Person> {
                Person.Tom::class with Person.Tom.serializer()
                Person.Jake::class with Person.Jake.serializer()
            }
        })
    val tom = Person.Tom("tom", "20")
    val wrapper = PersonWrapper("白", tom)
    val wrapperString = json.stringify(PersonWrapper.serializer(), wrapper)//序列化
    val wrapperObj = json.parse(PersonWrapper.serializer(), wrapperString)//反序列化
    println("wrapperString=$wrapperString}") 
    {"color":"白","person":{"type":"tom","age":"20","name":"tom"}}}
    println("wrapperObj=$wrapperObj}")
}
```

KS 多态序列化的相关链接: [https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md]()



不管是 MoShi 还是 KS ，序列化后的 string 都会有一个标记 type ,当然了，这个type 的名字是可改的。如果你项目网络请求的 json 参数和这个序列化 string 是一致的，那非常的棒，直接就可以做本地缓存和还原.如果你的网络请求是 {"color":"白","person":{"age":"20","name":"tom"}}} 这样子的，那么你可能需要做进一步的转换，比如拿到 反序列化后的 对象 ,然后再 new 一个 MutableMap< String, Any > 嵌套往里面一个一个 put 吧，最后让 Retrofit 提交这 Map.