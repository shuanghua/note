# 介绍

Handler MessageQueue Looper 在 Android 中板扮演着线程间传递消息的角色，最常见的就是在子线程和 UI 线程之间的消息交流

Handler 创建 Looper ,Looper 创建 MessageQueue

- Handler 发消息到 MessageQuesue 

- Looper 从 MessageQueue 取出 Message  给 Handler 的 handleMessage()

- Handler 处理 Message

一个线程一个 Looper

两个线程一个 Handler

> ThreadLocal<T> 是用来存储 [ 线程名 : T ] 对应关系的一个普通类

<!-- more -->

## 1.Handler

Handler 作用是对消息队列里的数据进行插入

```java
public Handler(@Nullable Callback callback, boolean async) {
    //...
    mLooper = Looper.myLooper();
    if (mLooper == null) {
        throw new RuntimeException(
            "Can't create handler inside thread " + Thread.currentThread()
                    + " that has not called Looper.prepare()");
    }
    mQueue = mLooper.mQueue;
    mCallback = callback;
    mAsynchronous = async;
}
```

> Looper.preare 创建 Loogper ，Looper 的创建顺便创建 MQ ，然后 Looper 被保存到 TL 中
> 
> 当 Handler 被创建，在 Handler 中使用的 Looper 实例是从 TL 获取的 Looper ，这样 Handler 就可以利用该 Looper 和 MessageQueue 来处理消息了；Handler 说："从现在开始我无敌了！"

 

### 消息的发送

 不管使用 sendMessage 还是 post 其最终是 Handler中 sendMessageDelayed()，然后调用 MessageQueue 中的 enqueueMessage() 方法

### 消息分发

 dispatchMessage(Message msg) ：先判断消息里是否含有 Callback,（这里的 Callback 其实是一个 Runable 类型，例如我们经常 post(new Runable) 这种写法），如果含有，那就将消息交给 handleCallback（）处理，不含有就重新派生一个handler, 并重写 handlerMessage进行处理。

### 消息接收

handlerMessage

## 2. Message

描述数据 和 数据本身的一个类，实现了 Parcelable 的序列化接口

Message 的构造函数是公开的，但最佳方法是调用 Message.obtain() 或者 Handler.obtainMessage() , 好处是能从回收对象池中复用

Message 是一个具有指向能力的数据类，加上 MessageQueue ，共同形成一个单向链表的数据结构

```java
public final class Message implements Parcelable {
    //...
    public int what;// 用来给 handler 区别数据
    public Object obj; // 传递的数据
    Bundle data;    // 传递的数据
    Handler target; // 对应的 handler
    Runnable callback;
    Message next; // 定义类似一个单向链表的数据结构
}
```

## 3.MessageQueue

MQ 在 Looper 构造函数中通过 new MessageQueue(quitAllowed) 创建,  创建的时间在 Looper.prepare() 调用时。

### 插入消息 enqueueMessage()

Handler 通过调用 MQ 的 enqueueMessage(Message msg, long when) 把 消息放入消息队列

```java
boolean enqueueMessage(Message msg, long when) {
    if (msg.target == null) { // 消息对应的handler为null
        throw new IllegalArgumentException("Message must have a target.");
    }
    synchronized (this) {
        if (msg.isInUse()) {//消息正在被使用
            throw new IllegalStateException(msg + " This message is already in use.");
        }
        if (mQuitting) {// handler 所在的线程已经被关闭
            IllegalStateException e = new IllegalStateException(
                    msg.target + " sending message to a Handler on a dead thread");
            Log.w(TAG, e.getMessage(), e);
            msg.recycle();
            return false;
        }
        msg.markInUse();
        msg.when = when;
        Message p = mMessages;
        boolean needWake;//是否需要唤醒队列

        // 消息队列中没有消息 或 新消息的延时为 0 或 新消息的延时小于上一个消息的延时
        // 意思新消息比上一个消息更紧急 
      if (p == null || when == 0 || when < p.when) {
            // 新的指向头，如果被封锁，则唤醒事件队列
            msg.next = p; //把上一个消息排在新消息的后面
            mMessages = msg;
            needWake = mBlocked;
        } else {
            // 大部分情况下，在队列的中间位置插入，我们不必唤醒
            // 当队列头部有障碍物并且该消息是队列中最早的异步消息，需要唤醒
            needWake = mBlocked && p.target == null && msg.isAsynchronous();
            Message prev;
            for (;;) {
                prev = p;
                p = p.next;
                if (p == null || when < p.when) {
                    break;
                }
                if (needWake && p.isAsynchronous()) {
                    needWake = false;
                }
            }
            msg.next = p; // invariant: p == prev.next
            prev.next = msg;
        }
        // We can assume mPtr != 0 because mQuitting is false.
        if (needWake) {
            nativeWake(mPtr);// 唤醒队列
        }
    }
    return true;
}
```

### 取出消息 next()

Message next() {}

返回数据也是一个循环,(这里的返回数据不是直接向线程需要数据的地方返回)，与存消息不同的是这个循环中多了个 nativePollOnce(ptr, nextPollTimeoutMillis) 阻塞方法， 这是 native 方法 ，一般在调用该方法之前会先调用 Binder.flushPendingCommands();

当 MQ 中消息为空时，就会阻塞当前线程，避免浪费 CPU 资源

## 4. Looper

Looper 被创建时（prepare()被调用），和线程名一起会被保存到 TL 中， 当 H 需要使用 Looper 时，就从 TL 中获取；因为 MQ 是在 Looper 中创建的，所以当 H 得到 Looper 时就可以通过 Looper.myQueue 获取到 MQ

> Looper.preare 创建 Loogper 实例 ，顺便创建 MQ ，然后 Looper 被保存到 TL 中，MQ 保存在 Looper 全局变量中。

 Looper 是一个死循环，在循环中调用 MQ 的 next 方法取出消息，然后结合 Handler 来分发处理，如果MQ里面的消息为空，则退出循环避免造成 cpu 资源浪费

### 创建

Looper.preare()：创建线程的 looper 对象

Looper.prepareMainLooper() : 创建主线程的 Looper 对象

> Looper 的构造函数有一个 quitAllowed 参数，最终传给 MQ , 当 quitAllowed = true ，MQ 可以停止被销毁的；MainLooper 的 MQ 默认传入 false ，意味这 Main 线程的 MQ 是不能被销毁的，因为 Main 线程运行的时间很长，需要不断的处理应用的各个事件或任务。

### 

### Looper开始工作

loop(): 里面是一个死循环，队列有消息就取出消息，并根据消息的 taget 交给对应的 Handler 分发处理 , 没有消息就 return 继续循环取下一个消息。

## 谈谈 MainLooper 的创建

首先 looper 的创建对应一个线程，MainLooper 对应的就是 Android 的主线程，主线程是在 ActivityThread 类的 main 函数(是的，他是 java 中的那个main 函数 )中创建启动的，所以 MainLooper 也是在这个时候被创建, ActivityThread 虽然名字中有一个 Thread ，但他不是继承自 Java Thread ，也可以说他不是要给真实的 Thread。

Activity的生命周期方法和系统事件，其实不是真正放在死循环中执行，而是定义了对应的消息标志，部分的消息标志被放在 ActivityThread 的一个内部类名叫 H 类中，这个 H 类 继承子 Handler ，所以他是一个 Handler ，在主线程的死循环下其实是通过发送对应的标记消息来触发生命周期方法或者事件。

死循环的问题:

   1.如何避免代码被重复调用执行?  设置消息标记来执行对应的代码

   2.如何保证线程长期运行而不占用 CPU 资源？ Android 中运用的是阻塞线程（ 在 MessageQueue.next() 中调用的 nativePollOnce() ）的方式，nativePollOnce 其实是Linux 操作系统 pipe/epoll 机制

3. ANR(Appcalition not respone ) 的发生原因? 某一个事件执行的时间大于 5 秒。这其实和死循环没有关系

> nativePollOnce 底层调用可以监视文件描述符中的 IO 事件， 当需要阻塞的时候在监视文件描述符上调用 epoll_wait，会让主线程在没有消息处理时就会阻塞在管道的读端，此时线程休眠不占用 CPU。同理的还有 Kotlin 协程的 Dispatcher.IO,  当只具有读写任务的挂起函数挂起时候在该 IO 线程不会消耗 CPU 资源，因此一定区分挂起函数是需要计算还是只需要读写，千万别无脑的把计算函数放到 IO 线程上执行，也千万不要把读写的挂起函数放到  Dispatcher.default 上运行
