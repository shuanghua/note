## 热流

流的实例会独立于收集器而存在, 当订阅者取消订阅，流也会存在，除非该实例被 Gc 回收了，StateFlow 和 SharedFlow 属于热流

### SharedFlow

共享流永远不会完成，共享流的接收端成为订阅者

共享流发送的是事件而不是数据

取消订阅通常通过取消协程的结构范围来实现，内部会在发送之前进行检查是否被取消

take 和 takeWhile 等具有截断流的操作符可以把共享流转换成完成流

shareIn 可以把冷流转换为共享流

共享流默认没有设置重放缓存

当有订阅者没准备好接收新的值时，共享流的发出者会挂起等待，知道所有订阅者都完成接收

没有订阅者时，发出的值会立即丢失

共享流是线程安全的，可以从并发协程中安全地调用，而无需外部同步

> 共享流的实现使用一个锁来确保线程安全，但暂停的收集器和发射器的冠状程序在这个锁之外被恢复，以避免在使用无限制的冠状程序时出现死锁。添加新的订阅者有O(1)的摊销成本，但发射者有O(N)的成本，其中N是订阅者的数量

#### 重放缓存：

共享流在有新的订阅者订阅时，会先发送缓存的值，然后再发最新的值

共享流的重放缓存像中间的一个订阅者，共享流每次都会先发送值给这个中间的订阅者，这样共享流就不需要在没有新订阅者的时候挂起等待

只有当至少有一个订阅者还没准备好接收新值时，才会发生缓冲区溢出的情况

没有订阅者时，共享流只缓存最新的值，永远不会发生缓冲去溢出情况

例如：
在默认情况下没有设置重放缓存，如果订阅者在发送者之后执行，那么订阅者将收不到发送者的事件；如果订阅者在发送者之前，才能收到发送者发送的事件
在设置了重放缓存的情况下：   如果订阅者在发送者之后执行，那么订阅者会先收到发送者提前放在缓存区的事件

可以通过是用 shareIn 来把冷流转换成热流

```kotlin
private val externalScope: CoroutineScope
val name: Flow<List<Dog>> = flow {
        //...
    }.shareIn(
        externalScope,
        replay = 1,
        started = SharingStarted.WhileSubscribed()
    )
```

- SharingStarted.WhileSubscribed()
  当存在订阅的时候才使上游生产方保持活跃状态

- SharingStarted.Eagerly()
  立即启动生产方，直接配发数据

- SharingStarted.Lazily()
  可在第一个订阅者出现后开始共享数据，并使数据流永远保持活跃状态

[Android 开发者网站的 SharedFlow 的使用介绍]("https://developer.android.com/kotlin/flow/stateflow-and-sharedflow")

### StateFlow

StateFlow 是一个状态容器 + 可观察数据的流 ，StateFlow 在消费端只收集到初始值和最后一个值
我们可以给 StaeFlow设置一个初始值

> SharedFlow 相比 StateFlow 可配置性更高

```kotlin
_uiState = MutableStateFlow(UiState.Success(emptyList()))
```

在 Android 中，StateFlow 非常适合用来发送需要让可变状态保持可观察的类，可以是 Ui 状态也可以是请求网络数据的状态。

StateFlow 和 LiveData 很像，内部都有一个 value 来更新数据，消费和生产都是相互独立的，
LiveData 能自动根据 view 的生命周期来取消和绑定消费方
StateFlow 的消费方只能放在 repeatOnLifecycle(Lifecycle.State.STARTED) 之类的感知块中来避免内存泄漏

Flow 的另一个优点是其提供了很多处理数据的操作符。

tateIn 中间运算符可以将其他的数据流转换为 StateFlow

## 冷流

与热流实例相反，当收集结束后冷流实例就会被销毁，当再次调用收集时就会重新触发创建生产方来提供流，是冷流只能用在一对一的发送和收集中

### Flow
