# Okhttp 设计示例代码

1. 创建 client 对象, 设置请求信息(地址, 请求头, 请求体, 请求方式等)
2. 创建 call , 添加到 准备队列（异步）, 或者运行队列（同步）
3. 如果是同步, 直接将 Call 添加到 runningSyncCalls 运行队列 ,然后直接调用 getResponseWithInterceptorChain() 责任链
4. 如果是异步, 直接将 AsyncCall 对象放入 readyAsyncCalls 准备队列, 当线程池有线程空闲时, 从队列中取出 AsyncCall (实现了 Runnable 接口) 对象放入线程池, 开启线程执行请求

```kotlin
// 对于异步请求队列最大存放 64 个请求
// 同一个 host 最大只允许 5 个请求
// 核心线程数（永远不被销毁的线程）为 0, 意味着 okhttp 的执行线程执行完成后就会被回收（没有任务时）
// 最大线程数为 Int.MAX_VALUE, 线程存活时间为 60 秒, 线程工厂为自定义的 threadFactory

// Dispatcher.kt  (异步请求才会用到这个方法)
private fun promoteAndExecute(): Boolean {
    val i = readyAsyncCalls.iterator()
    // 遍历准备队列 readyAsyncCalls, 取出一个 AsyncCall 对象
    while (i.hasNext()) {
        val asyncCall = i.next() // 从 readyAsyncCalls 队列中取出一个 AsyncCall 对象， 根据条件决定是否将其放入运行队列

        // 当前异步运行队列超过 64 个请求时, 结束整个 While 循环， 意味着请求队列已满，不能将该请求加入队列了
        if (runningAsyncCalls.size >= this.maxRequests) break

        // 同一个 host 最大只允许 5 个请求 , continue 继续取下一个 call 来判断 host
        if (asyncCall.callsPerHost.get() >= this.maxRequestsPerHost) continue
    
        i.remove() // 可以放入运行队列, 从准备队列中移除该 call
        isRunning = runningCallsCount() > 0
        executableCalls.add(asyncCall)
        runningAsyncCalls.add(asyncCall)
    }

    for (i in 0..<executableCalls.size) {
        val asyncCall = executableCalls[i]
        asyncCall.executeOn(executorService)
    }
    return isRunning
}
```

```kotlin
// Okhttp 架构伪代码
fun okHttpThreadPool() {
    // 异步请求需要使用线程池 (这里是根据 enqueue() 异步请求的路线设计, 异步请求的 getResponseWithInterceptorChain() 调用是在 run 方法中 , 同步是直接调用就行)
    val realCall = RealCall(okhttpClient = 0, originalRequest = 1)  // 一个 call 对象代表一个请求
    val response = realCall.getResponseWithInterceptorChain() // 开始请求

    // 最后将 response 丟给 Callback 接口的 onResponse 方法
}

class RealCall(
    val okhttpClient: Int,
    val originalRequest: Int,
) {
    fun getResponseWithInterceptorChain(): Response {
        // 创建拦截器链
        val interceptors = mutableListOf<Interceptor>()
        interceptors += okhttpClient.interceptors           // 用户自定义的拦截器集合
        interceptors += RetryAndFollowUpInterceptor(client) // 重试拦截器
        interceptors += BridgeInterceptor(client.cookieJar) // cookie 管理拦截器
        interceptors += CacheInterceptor(client.cache)      // 缓存拦截器
        interceptors += ConnectInterceptor                  // 连接拦截器
        if (!forWebSocket) {
            interceptors += client.networkInterceptors      // 网络拦截器
        }         
        interceptors += CallServerInterceptor(forWebSocket) // 调用服务器拦截器

        // 获取请求信息
        val request = 0  // 假如这是请求信息
        call++ // 每次加入一个请求，getResponseWithInterceptorChain（）hub 都会调用一次，call 变量记录了 call 的个数

        // 将请求信息传递给 拦截器链 （可以把 chain 理解为各个拦截器的管理器）
        val chain = RealInterceptorChain( // 第一次创建 chain
            request = request,
            call = call,
            interceptors = interceptors,
            index = 0
        )
        // 调用链管理器的 proceed 方法, 开始请求
        val response = chain.proceed(request) 
        // 后续只需要在每个拦截器中的 proceed 方法中调用 response = realChain.proceed(request) 就可以实现对下一个拦截器的调用
        return response
    }
}


// 函数接口: 使用 fun interface 关键字定义，只能包含一个抽象方法
fun interface Interceptor {
    fun intercept(chain: Chain): String

    // 匿名实现函数接口, 创建接口实例
    // 这里用于从最上层代码将 chain 传递到内部接口的 intercept 方法
    // 所以这里是最开始的入口
    companion object {
        inline operator fun invoke(
            crossinline block: (chain: Chain) -> String,
        ): Interceptor = Interceptor {
            block(it)
        }
    }

    // 这个接口的唯一实现类是 RealInterceptorChain
    interface Chain {
        // proceed 方法是拦截器 调用的入口
        fun proceed(request: Int): String

        // 触发 ConnectInterceptor
        fun request(): Request

        fun call(): Call

        // ...
    }
}


// RealInterceptorChain 保存了请求信息, call 对象, 拦截器集合, 以及当前拦截器的索引 index
// 实现 Chain 接口 中的所有抽象方法
class RealInterceptorChain(
    internal val request: Int, // 假如这是存放请求信息
    internal val call: Int, // 假如这是 call 对象
    private val index: Int, // 拦截器集合的索引
    private val interceptors: List<Interceptor>,// 拦截器集合
) : Interceptor.Chain {

    // 将当前全局属性拷贝到新的 RealInterceptorChain 实例中以实现复制，唯一的不同就是全局属性 index 会每次 +1
    // 利用当前数据创建新的拦截器链, 用于传递给下一个拦截器
    // 保证下一个拦截器的 request 与上一个拦截器的 response 一致
    internal fun copy(
        request: Int = this.request,
        call: Int = this.call,
        index: Int = this.index,
        interceptors: List<Interceptor> = this.interceptors,
    ) = RealInterceptorChain(request, call, index, interceptors)

    override fun request(): Request {
        return 0
    }

    override fun call(): Call {
        return 1
    }

    override fun proceed(request: Int): Response {
        check(index < interceptors.size) // 防止越界
        val nextChain = copy(index = index + 1, request = request) // 第二次创建 chain
        val interceptor = interceptors[index]

        // 将 chain 传递给下一个拦截器, 并返回 response
        val response = interceptor.intercept(nextChain)  // 将 chain 传给 函数接口的 intercept 方法, 并返回 response
        return response
    }

}

// 比如这里定义连接拦截器, 实现里就将任务委托给链的 request 方法
// 如果是别的拦截器就调用 内部接口的其它方法
object ConnectInterceptor : Interceptor {
    // 责任链管理类 RealInterceptorChain 会把 chain 实例传递过来
    override fun intercept(chain: Interceptor.Chain): String {
        val realChain = chain as RealInterceptorChain
        val connectedChain = chain.copy()
        return connectedChain.proceed(realChain.request)
    }
}
```


总结: 函数接口 持有 内部的普通接口,  这样的好处是利用了 函数接口 的唯一抽象方法, 
唯一抽象方法 做为入口, 实现了 委托模式, 使得代码更加简洁易于维护， 实现将复杂的逻辑交给内部接口处理。

okhttp 创建线程池代码在 Dispatcher.kt 文件中

```kotlin
  @get:Synchronized
  @get:JvmName("executorService") val executorService: ExecutorService
    get() {
      if (executorServiceOrNull == null) {
          // 核心线程数为 0, 最大线程数为 Int.MAX_VALUE, 线程存活时间为 60 秒, 线程工厂为自定义的 threadFactory
          // 核心线程数为 0 表示不创建永远不被销毁的线程, 最大线程数为 Int.MAX_VALUE 表示无限线程, 线程存活时间为 60 秒表示线程存活时间为 60 秒
        executorServiceOrNull = ThreadPoolExecutor(0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
            SynchronousQueue(), threadFactory("$okHttpName Dispatcher", false))
      }
      return executorServiceOrNull!!
    }
```
