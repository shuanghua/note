# Compose 的副作用 (Effect)
Compose 的副作 Api 与 Compose 函数的生命周期有很大的关联

问自己这几个问题：

1. 需要调用 suspend 函数吗？

是 -> 使用 LaunchedEffect。

2. 需要处理一个需要手动清理（如close(), unregister()）的 非 协程对象吗？

是 -> 使用 DisposableEffect。

3. 需要在每次 UI 更新后，都去通知或修改一个 Compose 之外的对象吗？

是 -> 谨慎地使用 SideEffect。

### LaunchedEffect

1. LaunchedEffect: 基于协程发射器，在组件消失时自动关闭协程
2. LaunchedEffect: 当想在 Composable 生命周期内运行 suspend 函数（协程）时使用, 例如：网络请求、数据库操作、显示Snackbar
3. LaunchedEffect: 在 Compose 函数首次组合的时候调用(第一次被绘制, key 发生变化也会被调用)，

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val isOffline by networkMonitor.collectAsState()


LaunchedEffect(isOffline) { // 括号内是触发条件, 一旦 isOffline 的值发生变化就会触发, Unit 通常代表触发一次
    if (isOffline) {
        snackbarHostState.showSnackbar(
            message = "网络离家出走了😭", 
            duration = SnackbarDuration.Indefinite
        )
    }
}
```



### DisposableEffect

1. DisposableEffect: 不是基于协程
2. DisposableEffect: 非智能, 需要手动调用 onDispose 来配合关闭相应资源, 如蓝牙监听, 通常需要监听完成后关闭蓝牙监听(如解除相应广播)
3. Compose 函数首次组合的时候调用(第一次被绘制, key 发生变化也会被调用)


```kotlin
val context = LocalContext.current
DisposableEffect(Unit) {
    // 注册广播接收器
    val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    val receiver = BluetoothStateReceiver()// 业务逻辑在 BluetoothStateReceiver 类中
    context.registerReceiver(receiver, filter)

    onDispose {
        context.unregisterReceiver(receiver)
        Log.d("CleanUp", "蓝牙监听器已回收")
    }
```



### SideEffect 

1. 不是基于协程
2. SideEffect: 需要将 compose 函数的数据提交给非 Compose 函数
3. Compose 函数每次重组时调用, 不关心 key 的变化(没有 key)
4. 通常用于数据分析库，需要知道用户当前正在看哪个图片

> SideEffect 的执行点是在 UI 已经被真实地更新到屏幕之后。而 ViewModel 的调用点是在状态更新之后，但在 UI 更新之前。

```kotlin
@Composable
fun UserTracker(user: User) {
    val analytics = remember { AnalyticsSDK() }

    // 每次界面刷新后更新用户标签
    SideEffect {
        analytics.setUserProperty("VIP等级", user.vipLevel)
        analytics.setUserProperty("最后活跃", LocalDateTime.now())
    }
}
```


### rememberCoroutineScope
LaunchedEffect 会自动触发, 且自身就是可组合函数, 如果想手动触发调用 suspend 函数或在非可组合函数中调用, 则使用 rememberCoroutineScope