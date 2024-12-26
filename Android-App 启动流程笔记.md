### zytego fork 出进程后:
- zytego 通过 socket 将 app 进程的 pid 传给 ams 

- zytego 反射调用 app 进程的 ActivityThread.java 中的 main 方法, 此时切换到 APP 进程

-  app 进程的 main 方法中启动 UI 线程 Looper 并将 ApplicationThread 回传到 AMS 中
```java
// ActivityThread.java    App 进程
main(){
    Looper.prepareMainLooper();
    ActivityThread thread = new ActivityThread(); 
    thread.attach(false, startSeq);
    Looper.loop();
}
```

```java
// ActivityThread.java    App 进程
attach(...){
   final IActivityManager mgr = ActivityManager.getService(); // IActivityManager 是一个 AIDL 接口,其实也就是 AMS
    try {
        mgr.attachApplication(mAppThread, startSeq); // 调用 AMS  的 attachApplication() 方法, 并把 App 进程的 applicationThread 传过去
    } catch (RemoteException ex) {
        throw ex.rethrowFromSystemServer();
    } 
}
```
  
- 此时代码的执行切换到 AMS 进程的 attachApplication() 方法

- AMS 的 attachApplicationLocked() 方法中结合 ApplicationThread 和 zytego 给的进程 id , 调用 App 进程的 bindApplication() 回到 App 进程进一步初始化 App
```java
// ActivityManagerService.java  系统服务进程
attachApplication(IApplicationThread thread, long startSeq){
    attachApplicationLocked(thread, callingPid, callingUid, startSeq);
}
```

```java
// ActivityManagerService.java  系统服务进程
attachApplicationLocked(...){
    thread.bindApplication(
        processName,     
        appInfo,
        app.sdkSandboxClientAppVolumeUuid, app.sdkSandboxClientAppPackage,
        providerList,
        instr2.mClass,
        profilerInfo, instr2.mArguments,
        instr2.mWatcher,
        instr2.mUiAutomationConnection, testMode,
        mBinderTransactionTrackingEnabled, enableTrackAllocation,
        isRestrictedBackupMode || !normalMode, app.isPersistent(),
        new Configuration(app.getWindowProcessController().getConfiguration()),
        app.getCompat(), getCommonServicesLocked(app.isolated),
        mCoreSettingsObserver.getCoreSettingsLocked(),
        buildSerial, autofillOptions, contentCaptureOptions,
        app.getDisabledCompatChanges(), serializedSystemFontMap,
        app.getStartElapsedTime(), app.getStartUptime()
        );   
    mAtmInternal.attachApplication(app.getWindowProcessController());

}
```

- 初始化好之后, AMS 利用 ActivityTaskManagerService 的 attachApplication() 方法处理第一个 Activity 相关的工作
```java
// ActivityTaskManagerService.java   系统服务进程
public boolean attachApplication(WindowProcessController wpc) throws RemoteException {
    return mRootWindowContainer.attachApplication(wpc);// 初始化 Applicaiton
}
```

```java
// RootWindowContainer.java  系统服务进程
class RootWindowContainer{
    boolean attachApplication() throws RemoteException {
        try {
            return mAttachApplicationHelper.process(app);
        } finally {
            mAttachApplicationHelper.reset();
        }
    }

    boolean process(WindowProcessController app) throws RemoteException {
        mApp = app;
        for (int displayNdx = getChildCount() - 1; displayNdx >= 0; --displayNdx) {
            getChildAt(displayNdx).forAllRootTasks(this);
            if (mRemoteException != null) {
                throw mRemoteException;
            }
        }
        if (!mHasActivityStarted) { // 目标 Activity 还没有启动， 最终触发下面的 test 方法
            ensureActivitiesVisible(null /* starting */, 0 /* configChanges */,
                    false /* preserveWindows */);
        }
        return mHasActivityStarted; 
    }

    private class AttachApplicationHelper implements Consumer<Task>, Predicate<ActivityRecord> {
        @Override
        public boolean test(ActivityRecord r) {
            // 启动 Activity
            if (mTaskSupervisor.realStartActivityLocked(r, mApp, mTop == r && r.getTask().canBeResumed(r), true)) {
                mHasActivityStarted = true;
}
        }
    }
}
```

# 1.AMS 解析请求
检查即将要启动的 Activity 进程是否已经被创建,  如果没有就是冷启动, 先通知 zytego fork app 进程, 然后创建 app 进程的 Application 实例, 下面就是详细的步骤:


### AMS 调用 App 的初始化
ams 根据 pid 获取 app 进程的 ProcessRecord (进程记录)
ams 通过 ApplicationThread 来调用 app 进程的 bindApplication(processName, appInfo, ...) 方法来进行 app 初始化, 从而完成 ams 和 app 的绑定工作.


### App 如何初始化 Applicatioin 实例
先来看看 app 进程的 ApplicationThread 中的 bindApplication 是如何初始化的?
将 ams 传过来的 processName 和 appInfo 等信息存储一个 AppBindData 实例中
然后通过 sendMessage 向 UI 线程发送一条带有 H.BIND_APPLICATION 标志的消息,消息内容是 AppBindData 实例, 因为这个是从 ams 调用过来的, 所以当前线程是 binder 线程池中的线程,也就是非UI 线程. 


### Application 和 Context 的创建绑定
UI 线程收到消息后, 获取应用的安装包的信息, 创建 Context 实例,  然后判断 Application 是否为空, 如果为空, 根据包名从缓存中查找(多进程下会有多个 Application ),  如果缓存中没有, 那么通过 xml 布局中设置 Application 标签内容反射创建 MyApplication, 如何用户没有自己创建 Application 的子类(假如是 MyApplication), 那么就反射创建 android.app.Application 的 Application ( 通过  classNmae..newInstance( ) 反射创建)
Application 实例创建好之后 , 回调 attach(context) 方法和 Context 进行绑定, 在 attach 中有立刻调用了 attachBaseContext(context)  , 到这里我们就知道为什么 attachBaseContext 方法比 onCreate() 方法更早执行了

> Context 主要就是负责给 App 提供访问系统资源的


### Applicaton onCreate 的回调
Application 创建完成之后, app 进程 通过 mInstrumentation.callApplicationOnCreate(applicaiton) 回调 Applicaiton 的 onCreate() 方法

# 2. AMS 分配 Activity 任务栈

## AMS 和 Activity 的启动

Zytego   
ActivityThread                  main()                           ->  mgr.attachApplication(mAppThread, startSeq);
ActivityManagerService          attachApplication()              ->  attachApplicationLocked()

```java
// 判断进程是否存在，如果不存在，则创建进程，并启动进程，
// 如果进程存在，则通过 thread.bindApplication() 回到 App 进程的 ApplicationThread 的 bindApplication() 方法去创建 Application 实例
// 最后回调 Application 的 onCreate() 方法
//
private boolean attachApplicationLocked(@NonNull IApplicationThread thread, int pid, int callingUid, long startSeq) {
    thread.bindApplication(...);  // 切回 App 进程的创建 Application 实例, 这部分属于 Application 的启动流程
    app.makeActive(thread, mProcessStats);// 记录进程信息，保存 App 进程 binder 句柄， 分别保存到了 ProcessRecord 和 WindowProcessController 中
    didSomething = mAtmInternal.attachApplication(app.getWindowProcessController());// 使用 ActivityTaskManagerService 处理第一个 Activity
}
```

ActivityManagerService                    attachApplicationLocked()        ->  mAtmInternal.attachApplication()
ActivityTaskManagerService.LocalService   attachApplication()              ->  mRootWindowContainer.attachApplication(wpc)



ActivityTaskManagerService  startActivity()  // 后续从 App 内启动其它 Activity 直接通过这个方法

ams 和 app 绑定完成之后, ams 就开始着手启动 activity 的操作, 在 system_server 进程中, 首先在 ActivityManagerService 的 attachApplicationLocked() 中通过 mAtmInternal.attachApplication() 来实现

mAtmInternal 其实就是 ActivityTaskManagerService , 在 App 进程对应的就是 ActivityTaskManager,  App 的第一个 Activity 启动完后，后续则只需要通过 ActivityTaskManagerService  启动 Activity 即可，无需再走 ActivityManagerService 的流程。

```java
// Instrumentation.java
public ActivityResult execStartActivity(){
    int result = ActivityTaskManager.getService().startActivity(...)
}
```

这段代码会进入到系统服务进程的 ActivityTaskManagerService (查看要启动的 Activity 进程是否已经被创建, 如果没有就是冷启动, 先创建 Application , 前面已经说明),这次 app 进程已经创建启动好了, 同时还判断 App 进程是否将 ApplicationThread ( binder 句柄 ) 注册到了 AMS , 如果都完成了, 就可以进入到分配 Activity 任务栈流程

# 真正切回 App 进程创建 Activity 实例

```java
// AMS 进程
// ActivityTaskSupervisor.java
boolean realStartActivityLocked(
    ActivityRecord r, WindowProcessController proc,
    boolean andResume, boolean checkConfig
) throws RemoteException {
    // 分配任务栈
    // 封装各个生命周期的Item, 如:LaunchActivityItem
}
```

# 3. AMS 启动 Acticity

ActivityManagerService 的 attachApplicationLocked 是和 App 交互的开始, mAtmInternal.attachApplication(app.getWindowProcessController()) 就是准备开始启动

之后进入到 RootWindowContainer.java (旧代码是 RootActivityContainer.java ) 的 attachApplication() 中, 显示要启动的 Activity (放入栈顶)

最后就进入到 ActivityTaskSupervisor 的 realStartActivityLocked(), 处理 ActivityLifecycleItem 后回到 App 进程创建启动 Activity

## 启动 Activity 代码

```java
// ActivityTaskSupervisor.java  系统服务进程
boolean realStartActivityLocked(
    ActivityRecord r, WindowProcessController proc,
    boolean andResume, boolean checkConfig
) throws RemoteException {
    // 如果要显示 lifecycleItem 等于 ResumeActivityItem
    // 如果要暂停 lifecycleItem 等于 PauseActivityItem
    // 通过 ClientLifecycleManager scheduleTransactionAndLifecycleItems 执行单个 item 事务,  最终通过 ClientTransaction   交给 app 进程
    // ClientTransaction 是一个 aidl , 对应实现是 ClientTransaction.java
     mService.getLifecycleManager().scheduleTransactionAndLifecycleItems(
                        proc.getThread(), launchActivityItem, lifecycleItem);
}
```

# 总结

ActivityTaskManagerInternal  是 ActivityTaskManagerService 的抽象, 所以看到 ActivityTaskManagerInternal , 直接去看  ActivityTaskManagerService 就行

attachApplication 是 attachApplicationLocked 的包装, 看到 Locked 结尾的就知道, 这个 Locked 结尾方法是被没有 Locked 结尾方法调用

















