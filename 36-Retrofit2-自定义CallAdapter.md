---
toc: false
comments: true
title: Retrofit2-自定义 CallAdapter
description: Retrofit2-自定义 CallAdapter
tags:
  - Android
id: 36
categories:
  - Android
date: 2018-5-28
---

# 第一步

> 以下代码来自：[https://github.com/googlesamples/android-architecture-components](https://github.com/googlesamples/android-architecture-components)


这里要把 **Call< JavaBean>** 包装成 **LiveData< ApiResponse< JavaBean>>** ,除了 LiveData 外，Apisponse 和 JavaBean 都是自己写的


<!-- more -->

#### 自定义 XXXCallAdapterFactoty 继承 Retrofit2.CallAdapter.Factory 
```kotlin
/**
 * 日后这里基本上就是直接复制粘贴
 * 像 Call 改成 LiveData Response 换成 ApiResponse
 * 只需要把 LiveData::class.java 和 ApiResponse::class.java 换成自定义的类型
 * 如果只需要自定义 Call ，请将下面的 ApiResponse 换成默认 Response
 * getRaw():通过参数拿到包装该参数的类型
 */
class LiveDataCallAdapterFactory : Factory() {
    //重写get函数
    override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
    ): CallAdapter<*, *>? {

        // 检查接口中函数的 Call 类型是否一致
        if (getRawType(returnType) != LiveData::class.java) return null

		// 检查 Call / LiveData
        if (observableType !is ParameterizedType) {
            throw IllegalArgumentException("resource must be parameterized")
        }

        // 获取默认 Response 类型 
        val observableType = getParameterUpperBound(0, returnType as ParameterizedType)

        // 获取 Response 位置的真实类型，这里是 ApiResponse
        val rawObservableType = getRawType(observableType)

        // 检查 Response / ApiResponse
        if (rawObservableType != ApiResponse::class.java) {
            throw IllegalArgumentException("type must be a resource")
        }

        // 通过 Call/ LiveData 来生成 body 类型
        val bodyType = Factory.getParameterUpperBound(0, observableType)

		// 返回自定义的 CallAdapter
        return LiveDataCallAdapter<Any>(bodyType)
    }
}
```


# 第二步
#### 自定义 XXXCallAdapter 继承 Retrofit2.CallAdapter

- 为了能接收到 Factory 传过来的 responseBody，别忘了给构造函数添加参数： val responseType: Type。
- 为了能接收到 Factory 传过来的 泛型 any ，记得定义泛型参数。
- 基类的 CallAdapter 需要传两个泛型参数: Factoty 传过来的 any; 第二个就是我们要包装成的类型 **LiveData< ApiResponse< R>>**。
```kotlin
/**
 * 为了能接收到 Factory 传过来的 responseBody
 * 别忘了添加构造函数的参数 val responseType: Type
 */
class LiveDataCallAdapter<R>(private val responseType: Type) :
        CallAdapter<R, LiveData<ApiResponse<R>>> {

    //Factory 传过来的 body
    override fun responseType() = responseType

	//该函数返回的就是我们要包装成的类型，这里返回 LiveData< ApiResponse< R>> 
	//所以直接创建一个 LiveData< ApiResponse< R>> 对象并将其返回
	//期间可以利用 LiveData 对象来处理各种调用逻辑
	//熟悉的 Call 对象，可以直接对数据进行操作
    override fun adapt(call: Call<R>): LiveData<ApiResponse<R>> {
        return object : LiveData<ApiResponse<R>>() {
            private var started = AtomicBoolean(false)//借助原子类来保证线程安全
            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {//确保里面代码不被多线程抢占
                    call.enqueue(object : Callback<R> {
                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            postValue(ApiResponse.create(response))
                        }

                        override fun onFailure(call: Call<R>, throwable: Throwable) {
                            postValue(ApiResponse.create(throwable))
                        }
                    })
                }
            }
        }
    }
}
```

再推荐一个类：[Retrofit2 + Kotlin 协程](https://github.com/twtstudio/WePeiYang-Android/blob/62bc1da7e34ecb4a8cc4592ef1a41af8059ef006/WePeiYang/commons/src/main/java/com/twt/wepeiyang/commons/experimental/network/CoroutineCallAdapter.kt)