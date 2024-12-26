# View 显示
什么时候去测量绘制是由底层信号说了算，但用户是可以随时操作屏幕频繁的刷新 View,  所以一定是先把用户的操作放到队列中，等到 Vsync 信号到来时才去执行 View 的测量, 布局, 绘制操作。

# Invalidate()

View 的 invalidate() 执行后, 先交给 父 View invalidate(), 然后交给 ViewParent 的 invalidateChild（），
最终交给 ViewRootImpl 的 scheduleTraversals() ，scheduleTraversals 中发送屏障消息，注册监听垂直同步信号，收到信号后开始交给 cpu 负责测量, 布局, 绘制, 之后在 RenderThread  然将数据提交给 gpu 进行栅格化,  并将数据缓存到 gpu buffer (back buffer 或 frame buffer) 中, 最后由 SurfaceFlinger 合成处理, 然后交给硬件显示到屏幕上.

# Vsync

Vsyn 是指垂直同步信号,  它是由显示器的刷新频率决定的,  即每秒刷新多少次,  通常是 60Hz 或 120Hz.
Vsyn 解决的是画面撕裂而不是卡顿的问题, 例如 测量,布局,绘制的时间太长,就会出现卡顿
当 View 的 invalidate() 执行后, 系统会将 view 标记为需要重新绘制, 同时注册垂直同步信号监听, 收到信号后才开始执行 测量-布局-绘制 的过程
可以看到 View 的 invalidate() 并不会立即执行, 而是等待 Vsync 信号到来后才开始执行.
收到 Vsync 信号后, Choreographer 还会通知 SurfaceFlinger 去读取 buffer (back buffer 或 frame buffer) 中的数据, 并合成处理, 显示到屏幕上.

# Choreographer

Choreographer 在软件层面负责注册和监听硬件层的 Vsync 信号,
每次 view 的 invalidate() 操作会进入到 ViewRootImpl 的 requestLayout() 方法中, 该方法会调用 Choreographer 的 postCallback() 方法, 并将 invalidate() 操作封装成 Runnable 对象, 并将 Runnable 对象加入到 Choreographer 的消息队列中. 然后注册监听Vsync 信号， 收到 Vsync 信号后从队列中取出这个 Runnable, 并执行 Runnable 中 run 方法里的任务.

# Choreographer 中的 CallbackQueue 数组

CallbackQueue 数组存的就是 Callback 类型,  刷新 View 有 刷新 View 的类型, 还要事件输入类型, 动画类型等等,
CallbackRecord 才是存放 View 刷新操作 Runnable 对象的队列,
CallbackQueue 类中持有 CallbackRecord

# SurfaceFlinger
cpu 将数据写到 back buffer , gpu 对 back buffer 进行栅格化，然后交换到 frame buffer， SurfaceFlinger 负责将 frame buffer 中的数据合成到屏幕上

# 总结
view 的 invalidate() 操作会进入到 ViewRootImpl 的 requestLayout() 方法中, requestLayout() 中, 向 Handler 发送屏障消息,  将 View 刷新操作的代码放到注册 Choreographer 的队列中,  Choreographer 会注册 Vsync 信号, Choreographer 收到 Vsync 信号后, 从队列中取出 View 的刷新操作代码, 取消同步屏障，执行 View 的测量, 布局, 绘制操作, 并将结果提交到 SurfaceFlinger 进行合成处理, 显示到屏幕上.

> Choreographer 每次都要重新注册 Vsync 信号.  因为有时候 View 是静止不变的, 这时候不需要一直监听 Vsync 信号,  而是等待 View 的状态发生变化, 然后再重新注册 Vsync 信号.


## 1. 检查线程 + 发送同步屏障消息
```java
// ViewRootImpl.java
class ViewRootImpl {
    public void requestLayout() {
        // 要想子线程更新 ui 不报异常,则 mHandlingLayoutInLayoutRequest 必须为 true
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread(); // 线程检查
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }

    // 将 View 刷新操作存入队列
    void scheduleTraversals() {
        if (!mTraversalScheduled) {
            mTraversalScheduled = true; // 设置标记, 防止过度绘制
            // 向 Handler 队列 post 同步屏障消息, 让 Handler 优先处理异步消息
            mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
            mChoreographer.postCallback(
                    Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
        }
    }
```


## 2. 将本次要测绘操作放入队列 + 订阅 Vsync 信号
```java
class Choreographer {
    // 将 View 刷新操作存入队列 + 订阅 Vsync 信号 ( Choreographer.java )
    private void postCallbackDelayedInternal(){
        mCallbackQueues[callbackType].addCallbackLocked(dueTime, action, token); // 加入队列
        scheduleFrameLocked(now);
    }

    // 订阅 Vsync 信号 ( Choreographer.java )
    private void scheduleFrameLocked(now) {
        if (isRunningOnLooperThreadLocked()) {
            scheduleVsyncLocked(); // 如果是 ui 线程, 立刻订阅 Vsync 信号，里面调用 native 方法去订阅 vsync 信号
        } else { // 否则发布消息以尽快从 UI 线程调度 vsync
            Message msg = mHandler.obtainMessage(MSG_DO_SCHEDULE_VSYNC);
            msg.setAsynchronous(true);
            mHandler.sendMessageAtFrontOfQueue(msg);
        }
    }
}
```

## 3. 收到 Vsync 信号 + 切换到主线程
```java
class Choreographer {
    class FrameDisplayEventReceiver extends DisplayEventReceiver
        implements Runnable {
        // 收到 Vsync 信号后, 通过 Handler 执行自己的 run() 方法
        public void onVsync(long timestamp, int builtInDisplayId) {
                Message msg = Message.obtain(mHandler, this);
                // 将该消息设置为异步, 消息队列发现存在同步屏障消息后, 会优先执行异步消息,
                // 从而让 View 刷新操作优先执行
                msg.setAsynchronous(true);
                mHandler.sendMessageAtTime(msg, timestampNanos / TimeUtils.NANOS_PER_MS);
        }

        // Ui 线程-将 View 刷新操作取出执行
        public void run() {
            mHavePendingVsync = false;
            doFrame(mTimestampNanos, mFrame, mLastVsyncEventData);
        }
    }

    // 将 View 刷新操作取出执行
    void doFrame(){
        // 事件输入
        mFrameInfo.markInputHandlingStart();
        doCallbacks(Choreographer.CALLBACK_INPUT, frameIntervalNanos);

        // 动画类型
        mFrameInfo.markAnimationsStart();
        doCallbacks(Choreographer.CALLBACK_ANIMATION, frameIntervalNanos);
        doCallbacks(Choreographer.CALLBACK_INSETS_ANIMATION, frameIntervalNanos);

        // View 的刷新绘制
        mFrameInfo.markPerformTraversalsStart();
        doCallbacks(Choreographer.CALLBACK_TRAVERSAL, frameIntervalNanos);

        doCallbacks(Choreographer.CALLBACK_COMMIT, frameIntervalNanos);
    }
}
```


## 4. 在主线程执行 View 的刷新绘制
```java
class Choreographer {
    void doCallbacks(int callbackType, long frameIntervalNanos) {
        for (CallbackRecord c = callbacks; c != null; c = c.next) {
            c.run(mFrameData); //  在 ui 线程取出 View 刷新操作并执行
        }
    }
}
```

```java
class ViewRootImpl {
    // 最终回到 ViewRootImpl 调用 View 的刷新绘制
    final class TraversalRunnable implements Runnable {
        public void run() {
            doTraversal();
        }
    }

    // View 的刷新绘制 ( ViewRootImpl.java )
    doTraversal(){
        // 取消标记 (该标记用了防止过度绘制)
        mTraversalScheduled = false;
        // 移除同步屏障
        mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);
        // 开始回调 View 测量, 布局, 绘制 方法
        performTraversals();
    }
}
```

