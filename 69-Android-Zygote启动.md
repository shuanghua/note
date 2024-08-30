## init.rc:

#### Zygote

Zygote 进程：由 init.rc 进程通过 fork + execve 系统调用二进制文件创建和启动出来的, 负责启动虚拟机, 注册jni函数, 预加载 系统资源(主题,常用类),
最后还循环监听 socket 消息(子进程和父进程的消息都可以监听并处理)


- SystemServer 进程: 由 zygote 父进程 fork 孵化得到,包括常用的 Java 函数,常用的系统服务,
如 ActivityManagerService(AMS), PackgeManagerService(PMS) , WindowManagerService(WMS) 等, 还调用 SystemServer.java 这个类, 再通过 Looper.prepareMainLooper() 启动 MainLooper


- App进程: app 进程通过 getService 获取 AMS 的 binder 对象，用该 binder 对象与 AMS 通信，AMS 收到消息后，AMS 用 socket 向 Zygote 发起“创建和启动”应用进程的请求,
Zygote fork 出应用新进程, Zygote同时向底层 binder 驱动申请创建 APP 进程的 binder
然后 Zygote 通过 socket 消息返回 App 进程的 app.pid 给 AMS,  并通过 AMS 传过来的 ActivityThread 类名来反射调用 ActivityThread 的 main 函数;
之后应用进程自己再向 AMS 注册提交一次自己进程的 binder 对象(app.thread), 这两步完成就代表应用进程成功完成启动


- ServiceManager: 单独的进程, 由 init.rc 进程 fork 和 execve 系统调用二进制文件创建和启动出来的,
主要负责系统服务的管理和创建 , 因为要处理 service 跨进程通信相关的任务, 所以它需要提前在 binder 线程池中注册启动自己的 binder 线程,
然后等待别的 service 来注册并作出响应.


- SurfaceFlinger: 单独的进程, 它需要用到 ServiceManager, 所以它需等待 ServiceManager 启动完成后才能正常初始化,
然后把自己注册到 ServiceManager 以能调用别的系统服务(或者被别的系统服务调用)


- Media:

> 区分应用层服务和系统层服务 

> binder: 打开 binder 驱动; 利用描述符来映射内存, 分配缓冲器; 向 binder 线程池注册; 最终返回App进程 binder 对象 ; 之后就可以利用这个 binder 对象去向 ServiceManager 进程注册添加服务 ;
同样的也是利用这个App进程 binder 对象 + 相对应的 service 名字去和对应的服务通信

> 应用进程的 binder 驱动是在应用进程创建启动之后打开的

> ApplicationContext 和 ActivtiyContext: 这两个的初始化流程都差不多, 先通过 classlocader 创建自身对象,
然后 new ContextImpl (如 crateBaseContextForActivity() ) 来生成 Context,
最后调用 attachBaseContext() 函数来连接 ContextWrapper 类中的 mBase 对象,
这样这个 context 就具有 ContextWrapper 的特性了, 剩下的就是回调生命周期, 返回自身对象

> ServiceContext 其实和 ApplicationContext 的创建更相似, 因为 Service 和 Application 都是没有界面的



- Activity 的启动:
```
void startSpecifiActivityLocked(ActivityRecord r,...){ //ActivityManagerService 层代码
    //检查 Activity 对应的进程是否启动,  有则启动对应的Activity,没有则去创建
  ProcessRecord app = mService.getProcessRecordLocked(r.processName);
  if(app != null && app.thread != null){//如果应用进程已经启动
      realStartActivityLocked(r,app,...); //启动Activity
      return;
  }
  mService.startProcessLocked(r.processName,...);
}
```
  
  > ActivityManagerService 发送 socket 消息来向 Zygote 发起启动应用进程请求; 收到请求后, zygote 孵化应用进程, 并打开 binder 驱动,
  应用进程孵化出来后, zygote 通过 socket 消息再返回所启动的应用进程 pid 给 AMS; 此时应用进程自身初始化完成,
  zygote 反射调用 ActivityThread 的 main 函数，在 main 函数中（也就是App进程）会通过 App进程的 binder ，向 AMS 注册自己的 App进程 binder(app.thread)， 完成进程创建;

  往后 AMS 和 应用进程就能通过这 binder 对象来进行通信(其中如: 应用进程向 AMS 提交 Appplication 对象; AMS 通知应用进程 bindApplication, AMS 通过 binder 调用应用进程启动具体的 Activity)
