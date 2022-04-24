## 基本使用
```kotlin
val client = OkHttpClient()// 里面也是通过建造者设计模式来创建的
val request = Request.Builder().url("http://").build()

val syncCall = client.newCall(request)
val response = syncCall.execute()//同步
    
val asyncCall = client.newCall(request)
asyncCall.enqueue(object : Callback {})//异步（线程池）
```
okhttpclient 和 request 实例都是通过建造者设计模式来创建的，request 指具体的任务请求，response 是请求后的响应， call 用于封装任务请求并发起请求



## OkHttp 中的责任链模式
okhttp 自带了5个拦截器（重试、桥接、 缓存、 连接、 网络请求 ） + 一个用户可自定义的拦截器来形成要给完整的责任链



## OkHttp Dispatcher
负责分发调度每个任务，任务队列的情况来配合线程池完成任务执行。













