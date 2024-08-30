---
title: Kotlin 协程-笔记
tags:
  - Kotlin
id: 16
categories:
  - Kotlin
toc: false
comments: true
description: 协程是一个轻量级的线程，可以挂起并稍后恢复。
date: 2017-8-27
---


- 协程不是线程，
- 协程是为了更好的处理代码逻辑，线程是为了更好的利用 CPU 资源。
- 在协程中可以使用线程
- 协程可以暂停、停止和恢复。
- 协程通过‘挂起函数’来支持，通常使用匿名的挂起函数。（如：suspend 修饰的 Lambda 表达式）
- 协程挂起和调度非常的轻量，但协程的创建需要一定的开销, 所以如非必要, 尽量不要在一个协程中再次启动另一个协程, 启动协程比启动线程更慢.

> 协同程序完全是通过编译技术实现（不需要 VM 或 OS 端的支持），并且通过代码转换进行暂停。主要就是将每个挂起函数转换为状态机，其中状态对应于挂起调用。在暂停之前，下一个状态存储在编译器生成类的相关的局部变量字段中。在恢复该协程后，恢复局部变量并且状态机在暂停后立即从状态继续

---


<!-- more -->

# 挂起函数 suspend

> 用 suspend 修饰的函数为挂起函数

- 当调用挂起函数时可能挂起协程
- 挂起函数只能在协程和其他的挂起函数中调用
- 启动协程，必须至少有一个挂起函数
- 通常我们可以把耗时操作或异步操作的函数挂起

## 挂起函数例子

```Kotlin
fun <T> async(block: suspend () -> T)
```
或者
```Kotlin
suspend fun foo(){
	//...
}
```
上面 async() 只是一个普通函数，它的作用事负责协程的启动工作，其参数 block 的类型是一个supend 修饰的 Lambda 表达式

- 如果挂起函数是抽象的，那么在实现的时候也必须是挂起的

```Kotlin
interface Base {
    suspend fun foo()
}

class Derived: Base {
    override suspend fun foo() { …… }
}
```

- 如果抽象函数不是挂起函数，那么在实现的时候也不能是挂起函数。

```Kotlin
interface Base {
    fun foo()
}

class Derived: Base {
    override suspend fun foo() { …… } // 错误
}
```
下面简单了解协程的大概启动流程：


# 协程标准 API 的三个主要方法

> 【注意】下面例子基于协程标准 API 来实现，只需要大概了解协程的封装原理，官方不推荐直接使用标准 API 来直接开发项目，但是我们应该了解其内部的封装原理，必要时候可以自己针对项目进行特色封装使用。

>  createCoroutine()
>  startCoroutine()
>  suspendCoroutine()

- 1.调用 startCoroutine() 传入一个 Continuation 来启动一个协程。

- 2.然后调用 suspendCoroutine<T> 函数来获取我们传入的 Continuation

- 3.需要将最终的结果传给 Continuation 的 resume() 函数来完成协程（通过 Continuation 实例调用 resume() 函数）

下面开始动手写一个协同程序，简称协程。


# 写一个协程
基于协程标准 API 来实现简单的网路下载协程


## 定义协程入口 startCoroutine
> 用 suspend 函数调用标准 API 中的 startCoroutine（）来开启一个协程。
> 同时这里我们使用的 suspend 修饰 Lambda 表达式来定义一个挂起函数。


```Kotlin
fun start( block: suspend () -> Unit) {
    block.startCoroutine(MyContinuation()) //传入 Continuation 启动协程
}
```
可以看到，协程的启动，其实就是通过用 supend 函数调用 startCoroutine() 来启动的。在调用时，它需要一个 Continuation 实例。下面就去创建这个 Continuation。


## 定义协程出口 Continuation
> 创建 Continuation，需要一个 CoroutineContext，也就是通常说的 **协程上下文**。

- 定义一个 **MyContinuation** 类，并实现 Continuation<T> 这个接口，重写相应变量和函数。
```kotlin
class MyContinuation() : Continuation<T> {

  override val context: CoroutineContext
      get() = EmptyCoroutineContext // 使用协程API自带的一个空 Context

	//协程出口
    override fun resume(value: T) {
        // 等待一个最终结果传给这个函数返回
    }

	//异常捕获
    override fun resumeWithException(exception: Throwable) {
        
    }
}
```
第一个变量是协程的上下文，我们给默认为空的上下文（EmptyCoroutineContext）凑合着用会儿。剩下的两个函数，就是协程工作完成后需要把结果传送出来的地方。

我们可以把 Continuation 当作协程的出口，当数据从出口出来之后就意味这这段协程结束（注意，一个协程可以有很多个 Continuation ，但多个 Continuation 一般都是串行拼接的，在启动协程时只能有一个入口和一个最终出口。


## 定义耗时任务 download()
下载图片的逻辑，比如利用 Okhttp 等下载。

```kotlin
fun download(url: String): T {
    //耗时代码，网络下载...
    return "假设这是小电影"
}
```
这里只是模拟下载，下载完成后返回相应的数据


## 将结果传出
> 通过 suspendCoroutine< T > 函数来获取我们的上面定义的 MyContinuation 实例，然后利用该实例调用 MyContinuation 里面的两个函数，最后将数据传出。

- 通过 continuation 传输数据
```kotlin
suspend fun <T> work(block: () -> T) = suspendCoroutine<T> {
  continuation -> continuation.resume(block()) //将最终结果传给上面的 resume()
}
```
我们得通过调用 API 的 suspendCoroutine() 函数（必须是在 suspend 环境下才能调用），拿到我们上面自己定义的 MyContinuation 实例， 然后利用该实例调用 MyContinuation 的 resume() 函数, 并将耗时工作得到的结果传给 resume( )，这样就完成了一个简单的协程封装。

> 只有被 suspend 修饰的函数才能调用到 suspendCoroutine() 函数，suspendCoroutine 函数的返回值就是我们 startCoroutine() 传入的 MyContinuation。


## 使用协程
使用我们上面写的协程
```kotlin
fun main(args: Array<String>) {
    start {
        work {
            download("www.baidu.com")
        }
    }
}
```
 写完，一脸懵逼，这么麻烦写这协程，而且还没有任何的优化实现，还不如老实用 Java 线程。因为这是基于标准 API 的实现，所以官方也不推荐直接使用。


> 上面的代码简单的封装了一个网络下载功能，这个网络下载非常的简洁，都没有线程相关，当运行之后会发现，它下载非常慢，会阻塞主线程。所以这里就要注意：**协程不是线程，协程不能取代线程，协程只是用来更方便处理异步任务**。假如使用线程来处理异步，线程的调度是 CPU 物理级别的，非常的难以控制，即使 Java 中有一些调度的 API,但用起来还是很不方便，况且这种操作 CPU 的调度，性能的消耗很严重，特别是电量的消耗。
> 所以协程最理想使用是配合线程一起使用，线程只管执行工作，协程来分配管理工作。


# 协程上下文 CoroutineContext
> 在协程中有一个非常重要的变量，就是协程的 Context, 协程上下文可以用来携带数据, 像携带 url、携带 Handler 各种数据，也可以用来切线程。

上面的代码我们直接将 URL地址直接传到了挂起函数中，如果 URL 是一个 var ，当协程里面有线程的话，这将是一个不安全的写法。所以下面我们修改代码，选择在协程的启动入口处传入我们的 url，同时为我们的协程添加线程支持，这样就不会阻塞主线程啦。


## 开始自定义协程的上下文
> 协程的上下文可以是多个的组合，多个协程上下文的传入是通过 + 好来拼接。
```Kotlin
fun start(
        urlContext: CoroutineContext = EmptyCoroutineContext,
        threadContext: AsyncContext = AsyncContext(),
        block: suspend () -> Unit
) {
    block.startCoroutine(MyContinuation(urlContext + threadContext))
}
```
urlContext 用来携带 url 地址用的，threadContext 就是我们的后台线程


- 协程的出口 MyContinuation
```kotlin
class MyContinuation(override val context: CoroutineContext = EmptyCoroutineContext) : Continuation<Unit> {

    override fun resume(value: Unit) {

    }
    override fun resumeWithException(exception: Throwable) {

    }
}
```
urlContext 是一个默认为空的协程上下文，AsyncContext() 是利用线程池来开启异步任务的上下文，最后将其通过 + 号组合起来传给 startCoroutine() 就完成了多个 Context 的协程工作。下面就来定义这两个 Context

下面就来看具体的代码实现：


## 定义 UrlContext
- 继承 AbstractCoroutineContextElement(Key)
```kotlin
class UrlContext(val url: String) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<UrlContext>
}
```
协程上下文必须有相应的一个 Key, 因为假如有多个上下文，程序才能区分。这里我们给 UrlContext 的 Key 是一个伴生对象定义的一个 Key。

## 定义 AsyncContext
- 同样继承 AbstractCoroutineContextElement（Key）
```kotlin
class AsyncContext : AbstractCoroutineContextElement(Key) {
  companion object Key : CoroutineContext.Key<AsyncContext>
}

class AsyncTaskThreadPool(val block: () -> Unit) {
    fun execute() = myThreadPool.execute(block)
}

private val myThreadPool by lazy {
    Executors.newCachedThreadPool()
}
```
> 这里的线程，我们直接弄个线程池来作为我们协程的线程支持。


## 封装到 work 中
> 想要在 suspendCoroutine 函数获取我们的 CoroutineContext,就需要一个 CoroutineContext 的 Receiver （扩展）
```kotlin
suspend fun <T> workForAsync(block: CoroutineContext.() -> T) = suspendCoroutine<T> { continuation ->
    AsyncTaskThreadPool {
        continuation.resume(block(continuation.context)) // 不扩展的话，这里用不了 continuation.context
    }.execute()
}
```
注意这里：block: CoroutineContext.() -> T，不扩展 CoroutineContext ,我们没办法通过 continuation 获取 context。


## 主函数调用
- 调用
```kotlin
fun main(args: Array<String>) {
    val url = "www.google.com"
    start(UrlContext(url)) {
        //主线程
        val s = workForAsync {
            println("workForAsync:${Thread.currentThread().name}")
            download(this[UrlContext]!!.url)
        }
        println("start:${Thread.currentThread().name}，s = $s")

    }
    println("请求的地址：$url")
}
fun download(url: String): String {
    Thread.sleep(10000)
    return "最终结果"
}
```
- log 输出
```
I/System.out: 请求的地址：www.google.com
I/System.out: workForAsync:pool-1-thread-1
I/System.out: start:pool-1-thread-1，s = 结果 // 10秒后打印
```

当把 println 函数去掉，就会发现非常的简洁，以后只需要写 download 函数往里面丢就行，当我们观察日志后发现结果是在子线程返回的，不用觉得意外，因为我们没有做线程的切换。

# 关于线程切换
> 上面将数据丢给 resume() 没有做线程切换，如果我们要在 Android 中将最终的数据传给到 UI 线程，那么就必须在 resume() 函数中做切换。这种线程之间传递数据的事，Kotlin 协程不会帮我们做，（这协程要你来有何用？）所以我们最终还是得自己写 Handler。

- 定义一个 SwitchThreadContinuation
```kotlin
class SwitchThreadContinuation<T>(val continuation: Continuation<T>) : Continuation<T>{
	override val context: CoroutineContext
        get() = continuation.context

    override fun resume(value: T) {
        切换到 UI 线程 {
            continuation.resume(value)
        }
    }

    override fun resumeWithException(exception: Throwable) {
        切换到 UI 线程{
            continuation.resumeWithException(exception)
	}
    }
}
```
因为最终的数据传到了 Continuation 的 resume（）, 所以只要在 resume 里面在包一层 Continuation 就行了。这一层 Continuation 专门负责作线程切换工作。这里就不具体写了，拿到最后的数据 value ，利用 Handler 传数据就好了。
这我们的协程中就有了两个 Continuation，那么编译器可不知道什么时候用哪个 Continuation。所以我们还得做拦截分发工作：将我们的 AsyncContext 实现 ContinuationInterceptor 拦截器，并实现其 interceptContinuation() 函数，然后将 ContinuationInterceptor 作为 AsyncContext 的 Key

- 实现 ContinuationInterceptor 拦截器
```kotlin
class AsyncContext : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    /**
     * continuation : 我们原始的 Continuation
     * 该方法返回的 Continuation 最终传给 suspendCoroutine()
     *
     * 先确保返回的都是我们的 ContextContinuation
     * 然后将确定好后得 ContextContinuation 传给 SwitchThreadContinuation 做线程切换工作
     * 最后由 SwitchThreadContinuation 将结果传给调用者
     */
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return SwitchThreadContinuation (continuation.context.fold(continuation) { continuation, element ->
            if (element != this && element is ContinuationInterceptor) {
                element.interceptContinuation(continuation)
            } else continuation
        })
    }
}
```

- interceptContinuation() 函数的参数是我们原始的 Continuation，其返回值是我们需要的 Continuation ，所以我们重写该函数就拦截得到我们需要的 Continuation，既然是切换线程，那就返回经过线程切换的 Continuation 。

- 最后，在挂起函数里面就得到最终的 SwitchThreadContinuation 对象，然后调用 resume() 抛出结果，完成整个协程任务。


#### 官方的线程切换的封装
定义一个 HandlerContext,详情见官方：[https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/kotlinx-coroutines-android/src/main/kotlin/kotlinx/coroutines/experimental/android/HandlerContext.kt](https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/kotlinx-coroutines-android/src/main/kotlin/kotlinx/coroutines/experimental/android/HandlerContext.kt "官方 HandlerContext") 这个类的实现

- 在启动协程时传入官方定义的 UI 对象，在主函数拿到对应的 context 对象，利用这个 context 再拿到 handler 做线程切换。


# 总结
- startCoroutine() 需要 Continuation() 对象

- Continuation() 必须包含一个 Context，可以使用 EmptyCoroutineContext 占位

- Context 必须直接或者间接实现了 CoroutineContext

- Context 间接实现 CoroutineContext，就是直接实现 AbstractCoroutineContextElement

- CoroutineContext: 协程的上下文，前面有说到这个上下文可以传很多个，通过 + 这个符号进行组合，组合的上下文可通过相应的 key 来获取。



# Kotlinx.Coroutine
首先 Kotlin 协程分为三层：
1. 底层 （语言支持层面）
2. 中间层（标准 API 层，官方不推荐直接使用标准 API 来开发项目，而应该使用基于该层封装好了的相关支持库），上面的我们就是针对这层的一个封装
3. 应用层 （像 Anko 、Kotlinx.Coroutine 等）

以上使用的是标准 API 来实现的协程，在产品项目中还得做好相应的封装工作（推荐还是直接使用官方 Kotlinx.Coroutine 这个高级层面的协程支持库），不然代码工程量还是挺大的，就为了下载张图片显示到 UI 上就这么费劲了<<-,没错，这就是基于标准 API 的开发，就像 Android 开发一样，都是 Java 语言，在没有 Android SDK 支持的环境下，纯利用 Java Api 去开发一个 Android 项目，可想难度之大。

> 因此官方就出了一个协程库 **Kotlinx.Coroutine**，以方便使用，其原理与上面的使用差不多，用这个库来实现下载一张图片就不用这么费劲了，同时还有适用于 Android 线程切换的 HandlerContext, 在这个库中，这些家常便饭都封装 好了,也就是上面的代码都不用写，只需要像在 Main 函数里那样调用就可以了。有了这个库，协程就叫使用协程，而不用写协程了。

> 在五花八门的项目中，没有一个库是完美的， 懂原理学会封装属于自己项目的协程还是很重要的。

- 关于 kotlinx.coroutine-core 这个库在文章后面的补充笔记有部分介绍，更详细的使用还是推荐官方库的 Github 首页介绍。[Kotlinx.Coroutine](https://github.com/Kotlin/kotlinx.coroutines)


# 协程补充笔记（2019-5-01）
> 线程的创建是一个大的开销，因此我们推荐在已有的线程上或者线程池上创建协程
> 协程的创建也是一个大的开销, 因此我们推荐在已有的协程中进行线程切换, 如 witchContext 等


#### runBlocking(){}
当前线程创建启动一个协程, 会阻塞当前线程, 一般只在 Main 函数和测试(测试类)函数中使用.
```kotlin
fun main(args: Array<String>) = runBlocking<Unit> {

}
```

#### launch(){}
> 括号中不传任何协程 Context，默认在当前线程创建启动一个协程，同时不阻塞当前线程，返回类型是 Job (不带有结果)


#### async(){}
> 括号中不传任何协程 Context，默认在当前线程创建启动一个协程，同时不阻塞当前线程，返回类型是 Deferred (可以带有结果)


#### GlobalScope.launch(){} 
在后台开启一个协程，可以理解为全局的协程，不会阻塞当前线程


## 协程结构块


#### withContext(context){}

withContext 不创建新的协程，可以在指定的 context 上执行， 阻塞当前协程， 不会阻塞当前线程

withContext 中出现异常， 其他的挂起函数也会被取消， 异常会传递到父协程， 如果最近的父协程不捕获，则传递到再上传层传递点（比如外层的 withContext,继续按前面方式找）， 如果顶层也不捕获， 在判断是否存在 CoroutineExceptionHandler 来处理异常。 如果还是没有捕获， 最终会导致红色崩溃异常。

```kotlin
// 必须有 suspend 关键字
suspend fun fib(x: Int): Int = withContext(CommonPool) {
        fibBlocking(x)
}
//递归调用（将耗时的递归函数单独抽出来，避免递归调用 withContext，造成多余的资源开销，虽然 withContext 占用的资源非常微小,比 lacunch 和 async 开销还要小的多）
fun fibBlocking(x: Int): Int = if (x <= 1) x else fibBlocking(x - 1) + fibBlocking(x - 2)
```

#### coroutineScope{}

coroutineScope 中出现异常或错误，则会取消内部的其他还没执行完成的协程， 异常会传递到父协程， 如果父协程不捕获，继续向上传递，如果有 CoroutineExceptionHandler 则交给 CoroutineExceptionHandler， 没有就直接抛出红色崩溃异常

coroutineScope 不创建新的协程， 不能设置自定义 context; 阻塞当前协程， 不会阻塞当前线程



#### supervisorScope{}

supervisorScope 中出现异常不会影响并取消其他领域的协程，

supervisorScope 不会重新抛出异常（也就是其他协程内部出现异常，层层向上传递碰到 supervisorScope， supervisorScope 会直接抛出这个异常， 不会再继续向上传递）， 但对于自己领域内的直接异常则是将异常传递到自己的父协程， 父协程可以选择是否捕获异常， 如果父协程不捕获，继续向上传递，如果有 CoroutineExceptionHandler 则交给 CoroutineExceptionHandler， 没有就直接抛出红色崩溃异常

### 异常总结
withContext  coroutineScope  supervisorScope  这三个函数其实都是异常传递的关键点， 但他们对异常转发的方式是不一样的
对于自己领域内的直接异常（看每个领域的是否是同一个上下文）， 这三个通常都会先传到父协程， 所以大多数情况， 我们可以使用 try catch 直接包住这 3个 就捕获到他们里面的异常


## 例子
#### 并行
```kotlin
suspend fun loadAndCombine（name1Url：String，name2Url：String）：Image = 
    coroutineScope  {
        val deferred1 = async  { loadImage（name1Url）} 
        val deferred2 = async  { loadImage（name2Url）} 
        combineImages（deferred1.await（），deferred2.await（） ）
    }
    // deferred1 和 deferred2 是并行处理的；这里和 coroutineScope 没有关系，coroutineScope 在这里只是为了假设任务1出现了异常，同时取消任务2 这个需要根据项目需要而定的.
```
上面代码中 deferred2 有可能比 deferred1 现完成, 因为只有 async 的情况下不会阻塞当前协程(当调用 deferred2.await() 时就会阻塞当前协程 ), 所以这是并行的。await() 的调用位置很重要哦.


#### 串行
```kotlin
    val v1 = async(start = CoroutineStart.LAZY) {//这里秀一下懒加载
      work1()
    }
    v1.await() //阻塞当前协程, 只有先等待 v1 执行完成才能开始执行 v2
    val v2 = async(start = CoroutineStart.LAZY) {
      work2()
    }
    v2.await()
```


#### delay，
没有指定 delay 暂停时间差的情况下，默认普通代码比协程代码优先被执行；以下代码演示通过使用 CoroutineStart.UNDISPATCHED 开启优先执行协程代码的的情况，执行协程代码会优先执行，直到遇到第一个 delay ，然后立即执行非协程代码。
```kotlin
fun setup(hello: Text, fab: Circle) {
    fab.onMouseClicked = EventHandler {
        println("Before launch")
        launch(UI, CoroutineStart.UNDISPATCHED) {
            println("Inside coroutine")
            delay(100)
            println("After delay")
        }
        println("After launch")
    }
}
执行结果：
Before launch
Inside coroutine
After launch
After delay
```


## Coroutines UI 在 Android 中的使用
- 让 Job 和 Activity 的生命周期绑定例子
```kotlin
class MainActivity : AppCompatActivity(), JobHolder {
    override val job: Job
        get() = Job()

    val View.contextJob: Job?
        get() = (context as? JobHolder)?.job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.onClick {
        }
    }

    fun View.onClick(action: suspend () -> Unit) {
        val evenActor = actor<Unit>(UI, parent = contextJob, capacity = Channel.CONFLATED) {
            for (event in channel) action()
        }

        setOnClickListener { evenActor.offer(Unit) }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
interface JobHolder {
    val job: Job
}
```

- Android 中避免在 UI 线程执行耗时操作，下面的代码会导致 UI 卡顿，严重则导致 UI 冻结，然后 ANR 异常
```kotlin
//模拟非常耗时的任务
fun  fib（x ： Int）： Int  = if（x <=  1）x else fib（x -  1）+ fib（x -  2）
```
- 
```kotlin
fun setup(text: TextView, button: Button) {
    var result = "none" // the last result
    // counting animation 
    launch(UI) {//模拟 Android UI 线程
        var counter = 0
        while (true) {
            text.text = "${++counter}: $result"//模拟系统不断刷新UI
            delay(100)
        }
    }
    // 点击一个按钮开始耗时任务
    var x = 1
    button.onClick {
        result = "fib($x) = ${fib(x)}"
        x++
    }
}
```

解决就是，将耗时的代码放到子线程中执行。
> 协程不是线程，协程是方便我们处理异步，比如各个任务之间的调度。协程配合线程，协程能在同一条线程上执行非常多的任务，而这一条线程不应该是 Android 的 UI 线程。


- 避免重复开启协程
```kotlin
setOnClickListener { 
    launch(UI) {
        action()
    }
}
```
上面代码，当用户无聊连续点击，每次都开启新协程，而此时可能上一协程任务还没执行完呢，最终造成在短时间内并发执行了很多个协程。

解决：在上一个协程没有完成，开启新的协程（最多只并发一个协程工作），我们利用 actor 这个函数来处理这种情况，当上一个事件还没有处理，你给他发送新事件，它会丢弃掉这个新的事件。反之，actor 接收新事件，然后立即发送给 Channel
- 
```kotlin
fun View.onClick(action: suspend (View) -> Unit) {
    // launch one actor
    val eventActor = actor<View>(UI) {//创建 actor 
        for (event in channel) action(event)//遍历 Channel 中的事件
    }
    // install a listener to activate this actor
    setOnClickListener { 
        eventActor.offer(it)//发送新事件
    }
}
```


- 将多个事件合并处理
```kotlin
fun View.onClick(action: suspend (View) -> Unit) {
    // launch one actor
    val eventActor = actor<View>(UI, capacity = Channel.CONFLATED) {//capacity = Channel.CONFLATED 合并多个事件
        for (event in channel) action(event)//遍历 Channel 中的事件
    }
    // install a listener to activate this actor
    setOnClickListener { 
        eventActor.offer(it)//发送新事件
    }
}
```
上面的代码，合并的是最近的收到事件，其最后在上一个事件执行完成立即执行下一个事件，当重复多次点击，其也只会执行最后点击的一两次事件。不会是点击多少次，执行多少次。

> 处理 capacity = Channel.CONFLATED 外，还有 capacity = Channel.UNLIMITED，UNLIMITED 会缓存连续收到的事件，然后在收到最后一次事件后，执行缓存的所有事件。









































 


