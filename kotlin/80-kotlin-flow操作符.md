```kotlin
val sum = (1..5).asFlow()
    .map { it * it }                         
    .reduce { a, b -> a + b }
println(sum)
```

> emit : 
> 发送值，每调用一次就会执行一次 collect {}

- map :
  转换操作符

- filter ：
  根据条件过滤

- take(2) : 
  只向下游发送序列的第一个和第二个值

- transform : 
  转换操作符，这个操作符中可以调用多个 emit 

- flowOn ：
  更改上游 flow 的协程上下文环境，类似切换线程

- buffer()

- onStart
  当上游 flow 开始的时候调用，还没向下游发送值的时候，可以用来做一些初始化操作

- onEach
  上游向下游发送的“每个值”之前，都会调用 onEach 里面的代码
  
  ```
  fun simple(): Flow<Int> = flow {
    for (i in 1..3) {
        delay(100) // pretend we are asynchronously waiting 100 ms
        emit(i) // emit next value
    }
  }
  val time = measureTimeMillis {
    simple()
        .buffer() // 当 emit 第一次发送时碰到 buffer 就告诉 simple() 后面的值可以无视 delay(100) , 因为有缓冲的存在
        .collect { value -> 
            delay(300) // 缓冲完成后每个 300 毫秒输出一次
            println(value) 
        } 
  }
  ```

- conflate() : 
  当下游处理太慢，将丢弃需要等待的值，只向下游发送不需要等待的值

- collectLatest:
  当下游处理太慢则只收集最后不需要等待的那个值，但如果下游处理很快就和 collect 是一样的，通常在需要处理两种情况时使用
  
  ```kotlin
  val time = measureTimeMillis {
    simple()
        .collectLatest { value -> // cancel & restart on the latest value
            println("Collecting $value") 
            delay(300) // pretend we are processing it for 300 ms
            println("Done $value") 
        } 
  } 
  Collecting 1
  Collecting 2
  Collecting 3
  Done 3
  ```

- zip{a,b} :
  将两个 flow 序列根据给定的逻辑（可以类型转换，类似map）合并成一个新的 flow 序列, 如果其中的一个 flow 生产很慢，那么另一个会等待这个生产慢的flow，然后再合并.

- combine:
  和 zip 类似， 不同的是，快的 flow 不会等待慢的 flow ，快的 flow 值会和慢 flow 已经生成的最新值继续合并，最后传给 collect

## 扁平 Flow

> 结合官方文档并通过 图片80-flow-flattenMerge.png 来理解，大概就是把被扁平的流与另外两组 emit 组合起来理解

- flatMapConcat:
  以顺序的方式将给定的流扁平化为单个流，而无需交错嵌套流。 内部流是由这个运算符按顺序收集的（等待 + 不缓存）

- flattenMerge:
  将给定的流扁平化为单个流，同时限制并发收集的流的数量。在 concurrency == 1 时，此运算符与 flattenConcat 相同 （ 等待 + 缓存 ）
  (当扁平的 '流1' 遇到需要等待的 emit （流B）时，会缓存扁平的 '流1' ，当 流B 等待时间到后再发送 流1B，在缓存等待的过程中被扁平的流会继续发送 '流2' )

- flatMapLatest
  先执行不需要等待的 emit，当最后新的流发出，就取消对以前流的收集 （ 丢弃除了最后一个以外的所有等待 + 保证最后的一个流）

## 终端操作符

终端运算符可以直接让 Flow 返回该操作符的结果

- reduce : 把上游传下来的序列值根据运算符号来相加、相减、相乘或者相除，例如 a+b 代表 1 + 2 + 3 + 4 + 5 【终端操作符】

- fold: 设定一个初始值，每次 （a+b）的结果都会与该值相加一次 【终端操作符】

- first : 确保序列只发出第一个值 【终端运算符】

- single ： 确保序列只发出一个且只有一个的值 【终端操作符】

- toList :  【终端操作符】

- toSet :  【终端操作符】

## flow 异常

- collect:
  只能捕获上游的异常，不能捕获下游的异常

- 声明式捕获异常：
  我们可以把 catch操作符 的声明性和处理所有异常的愿望结合起来，把 collect 操作符的中的代码移到 onEach中，这样 collect 就在 catch 操作符 的上游。这个流程的必须通过调用不带参数的 collect() 来触发。
  
  ```kotlin
  simple()
    .onEach { value ->
        check(value <= 1) { "Collected $value" }                 
        println(value) 
    }
    .catch { e -> println("Caught $e") }
    .collect()
  ```

## flow 发送和接收

> 由于性能问题，通过扩展调用时，不能使用 cancel() 来取消 flow，如 （1..2).asFlow.collect{ cancel() } ; 但是如果你再加上 .cancellable() 就不一样了，加了 .cancellable() 就可以让 cancel 生效。如 (1..5).asFlow().cancellable().collect { cancel() }

- asFlow: 
  不用写 emit 来发送流

- onCompletion：
  当收集完成后调用，我们还可以在 onCompletion 中判断异常情况，无论是上游异常还是下游异常，onCompletion 都能感知到，需要根据其参数来判断，如果参数不为空，说明最后完成是存在异常的，onCompletion 自身不捕获异常，所以我们应该再加上 .catch{} 来捕获异常

- collect：
  收集流  （终端操作符）

- .onEach ：
  中间操作符，返回一个流，在上游流的每个值被排放到下游之前调用给定的动作。通常临时用来在发送端打印个日志查看流序情况或者调用 delay() ，而不用每次都在收集时打印

> 仅将一个中间运算符应用于数据流不会启动数据流的收集。

- launchIn(this):
  
  ```kotlin
  fun main() = runBlocking<Unit> {
    events()
        .onEach { event -> println("Event: $event") }
        .launchIn(this) // 在一个单独的程序中启动流程
    println("Done")
  } 
  Done    //先输出
  Event: 1
  Event: 2
  Event: 3
  ```

- callbackFlow:
  将回调转换成 Flow ，推荐在需要连续回调结果的情况下使用，如间隔性的实时定位回调，对于不需要重复性的单次回调请使用 suspendCancellableCoroutine 。

```kotlin
fun flowFrom(api: CallbackBasedApi): Flow<T> = callbackFlow {
    val callback = object : Callback { 
        override fun onNextValue(value: T) {
            trySendBlocking(value)  //  通过 trySendBlocking 把回调的结果发送到流
                .onFailure { throwable ->
                    // 如果下游已被取消或出现失败情况
                }
        }
        override fun onApiError(cause: Throwable) {
            cancel(CancellationException("API Error", cause))
        }
        override fun onCompleted() = channel.close()
    }
    api.register(callback)
    awaitClose { api.unregister(callback) }
}
```
