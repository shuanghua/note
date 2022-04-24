---
toc: false
comments: false
title: ViewModel LiveData 笔记
description:  ViewModel LiveData 笔记
tags:
  - Android
id: 44
categories:
  - Android
date: 2018-9-30
---

## ViewModel 不要持有 View Activity Fragment 等这些生命周期短的实例，以免造成内存泄漏。

看官方的 ViewModel 生命周期图，我们都知道 ViewModel 的生命周期和 Activity 或者 Fragment 差不多，但有一个例外： 屏幕旋转的时候， Activity 会结束生命然后再重建生命，而此时 ViewModel 的生命依然存活；ViewModel 的生命存活时间大于了 Activity 的生命，该情况下，如果 ViewModel 持有了 Activity 的实例，就很容易造成内存泄漏。 


<!-- more -->

## ViewModel LiveData 需要注意的问题
> 使用 ViewModel + LiveData + Navgation 官方导航组建进行 Framgent 跳转

#### 问题
我遇到的一个问题：从 FramgnetA navgate 跳转到 FragmentB, 然后再从 FramgentB popBackStack 或者 popBackStack 返回到 FragmentA,然后 LiveData 的 observer 观察执行了多次, 假设在 observer 中 set 数据到UI，这对性能，以及绘制的影响非常巨大。

#### 原因
LiveData observe 源码
```java
    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
        assertMainThread("observe");
        if (owner.getLifecycle().getCurrentState() == DESTROYED) {
            // ignore
            return;
        }
        LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
        ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper);
        if (existing != null && !existing.isAttachedTo(owner)) {
            throw new IllegalArgumentException("Cannot add the same observer"
                    + " with different lifecycles");
        }
        if (existing != null) {
            return;
        }
        owner.getLifecycle().addObserver(wrapper);
    }
```

通过看源码 LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer) 这行知道：当我第一次启动 FragmentA 的时候，LifecycleBoundObserver 这个类保存了我的 FragmentA 引用和对应的观察者。


往下看到 ObserverWrapper existing = mObservers.putIfAbsent(observer, wrapper) 这行的时候知道，mObservers 保存了 observer 
这个观察者对象和上面包装对象 wrapper。所以 mObservers 结构大概是这样子的：observer -> (owner, observer)


往下继续看 	if (existing != null && !existing.isAttachedTo(owner)) ，就会发现，如果 existing 不为空的时候则直接 retrun 跳出，说明 existing != null 时这个观察者曾经已经添加过了; 而 existing 等不等于 null, 取决于 mObservers.putIfAbsent(observer, wrapper)，在这个 putIfAbsent() 方法里面，当作为 key 的 observer 相同的时候， existing!=null (此时要么报异常要么推测当前方法);  key 不同的时候则 existing == null （或者理解为这个 key 没有对应的值时，existing==null）。因此可以看出 如果两次传进来 observer 不是同一个对象，但 owner 相同， owner.getLifecycle().addObserver(wrapper) 这行代码一定会执行，最终造成了一个 owner 下的观察多次被调用。


回到我遇到的问题场景：当我从 FragmentA 启动到 FragmentB ，被隐藏掉的 FragmentA  并没有从 LiveData 的中清除掉对应的 owner 和 observer (为了还原数据，例如旋转屏幕)，又因为 FragmentA 显示时是从导航栈中置顶显示，从来没有出栈过，所以第二次的 owner 和第一次是同一块内存对象，从 FragmentB 返回到 FragmentA 后又再一次的添加了观察（又 new 了一遍 Observer）, 并把这个新的 observer 添加到了 mObservers 中，出现了一对多的关系。
> owner 能观察到 Fragment 或 Activity 的完整生命周期，包括屏幕旋转。正常的 Fragment 跳转，并不是销毁原先的 Fragment。所以 Fragment 只要在导航栈内，其 owner 就会一直和 LiveData 绑定。owner 还真是一个神奇的玩意儿。



#### 总结
- owner 不同的情况下: 传入相同的 Observer 会触发异常，不同的 Observer 只会执行一次 （因为 owner 不同）

- owner 相同的情况下: 传入相同的 Observer 只执行一次 , 不同 Observer 执行多次 （一个 owner 对应了多个 observer）




#### 解决
 1: 在跳转到 FragmentB 之前先移除 FragmentA 下的所有 LiveData 观察,这样就不会重复 add 观察事件
 ```kotlin
LiveData.removeObservers(this)
Navigation.findNavController(activity!!, R.id.my_nav_fragment).navigate(R.id.b_fragment)
```

 2: 根据上面的总结修改相应的对象,比如自定义 Observer 
```kotlin
import androidx.lifecycle.Observer

open class Event<out T>(private val content: T?) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 检查当前事件中的值
     */
    fun peekContent(): T? = content
}

/**
 * 为了避免每次都调用 getContentIfNotHandled  获取内容
 * 所以封装了一个自己的 Observer,
 * 最终在观察的时候，能直接拿到具体的内容值，而不是 Event 包装的值
 */
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let { //如果内容值为 null ，则不发送消息
            onEventUnhandledContent(it)
        }
    }
}

//仅用于个人笔记
class TestObserver<T>(private val content: (T) -> Unit) : Observer<T> {
    override fun onChanged(event: T) {
        content(event)
    }
}
```



 > 断点或打印各个对象的分配情况，注意在出现多次回调时他们的变化