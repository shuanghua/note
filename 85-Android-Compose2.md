Android 应用开发有三个坑位： 程序进入后台，屏幕旋转，进程重启。ViewModel 默认只能处理前面的两个

## 状态

[Android Compose 官方的中文版文档 ](https://developer.android.com/jetpack/compose/state)中对状态的说明很多都是用专业名词来解释，和用翻译插件来看英文版文档没多大差别，对新手不太友好。其实对 ”状态”，完全可以理解为离 Ui 最近的数据。数据的来源可以放在 ViewModel(ViewModel 作为可信来源)、可以单独抽离到一个普通类（状态容器）、rememberXXXX("数据源") 。最后一种通常放在可组合函数中，也叫可组合项（即将可组合项作为可信来源）

> 所以 “状态” 其实就是数据，通常 remember + state 来定义。
> 
> 数据类型必须是 Compose 能观察的，比如 State<List<T>> 或者 listOf ，像 LiveData 就不行，LiveData 必须通过调用 LiveData.observeAsState() 之后才行。

```kotlin
    val strState = remember {mutableStateOf(name)}  // state
    var value2 by remember {mutableStateOf(name)}  // var value
    val (value3, setValue) = remember {mutableStateOf(name)} // value setValue
```

```kotlin
@Composable
fun Greeting(name: String) {
    var configValue by rememberSaveable {mutableStateOf(name)}
    Column() {
        Log.d("M", "----------${configValue}")
        Text(text = "Hello $strState!")
        Button(onClick = {configValue += "${name.length}"}) {
            Text(text = configValue)
        }
    }
}
```

每当点击 Button 会触发 onClick 中对 configValue 赋值的代码，configValue 是 remember 类型，所以只要其他地方使用了 configValue ，最终获得的都是赋值之后的内容。这就是 remember 的作用，remember 其实就是维护了一个在可组合函数的数据源，只要这个数据源在可组合函数内部，我们就称之为有状态的 Compose 函数

相反如果 configValue 不是 remember 类型，则使用 configValue 时都只能获取到第一次的默认值，就算把 Button 点击到爆，数据也不会改变，此时也就是说 Compose 函数没有状态, 但如果像函数无状态又像能改变数据，那么就需要通过提升状态把数据源放到外部的另一个 Compose 函数中。

## 笔记

对象序列化1

```kotlin
@Parcelize
data class City(val name: String, val country: String) : Parcelable

@Composable
fun CityScreen() {
    var selectedCity = rememberSaveable {
        mutableStateOf(City("Madrid", "Spain"))
    }
}
```

对象序列化2

```kotlin
data class City(val name: String, val country: String)

val CitySaver = run {
    val nameKey = "Name"
    val countryKey = "Country"
    mapSaver(
        save = { mapOf(nameKey to it.name, countryKey to it.country) },
        restore = { City(it[nameKey] as String, it[countryKey] as String) }
    )
}
@Composable
fun CityScreen() {
    var selectedCity = rememberSaveable(stateSaver = CitySaver) {
        mutableStateOf(City("Madrid", "Spain"))
    }
}
```

对象序列化3

```kotlin
data class City(val name: String, val country: String)

val CitySaver = listSaver<City, Any>(
    save = { listOf(it.name, it.country) },
    restore = { City(it[0] as String, it[1] as String) }
)

@Composable
fun CityScreen() {
    var selectedCity = rememberSaveable(stateSaver = CitySaver) {
        mutableStateOf(City("Madrid", "Spain"))
    }
}
```

## Scaffold 上的 Snackbar

通过点击 Button 显示的 snackbar

```kotlin
@Composable
fun MoviesScreen(scaffoldState: ScaffoldState = rememberScaffoldState()) {

    // 创建一个与 MoviesScreen() 的生命周期绑定的 CoroutineScope。
    val scope = rememberCoroutineScope()

    Scaffold(scaffoldState = scaffoldState) {
        Column {
            Button(
                onClick = {
                    // 在事件处理程序中创建一个新的协程，以显示 Snackbar
                    scope.launch {
                        scaffoldState.snackbarHostState
                            .showSnackbar("Something happened!")
                    }
                }
            ) {
                Text("Press me")
            }
        }
    }
}
```

通过外部控制的 snackbar

```kotlin
interface UiState<T> {
    val value: T?
    val exception: Throwable?
    val hasError: Boolean
        get() = exception != null
}
data class Movie(val name: String, val id: String)

@Composable
fun MyScreen(
    state: UiState<Movie>,
    scaffoldState: ScaffoldState = rememberScaffoldState()
) {
    if(state.hasError) {
        LaunchedEffect(key1 = scaffoldState.snackbarHostState) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = "Error message",
                actionLabel = "Retry message"
            )
        }

        Scaffold(scaffoldState = scaffoldState) {
            ShowMovie()
        }
    }
}
```

## produceState（生产 Compose 数据）

produceState ：将非 Compose 数据转换为 Compose 支持的数据 State<T>

```kotlin
@Composable
fun loadNetworkImage( //应采用 Kotlin 常规函数命名方式命名,以小写字母开头的命名
    url: String,
    imageRepository: ImageRepository
): State<Result<Image>> {
    return produceState<Result<Image>>(initialValue = Result.Loading, url, imageRepository) {

        val image = imageRepository.load(url)

        value = if (image == null) {
            Result.Error
        } else {
            Result.Success(image)
        }
    }
}
```

## derivedStateOf

将一个或多个数据对象转换为其他数据对象

```kotlin
@Composable
fun TodoList(highPriorityKeywords: List<String> = listOf("Review", "Unblock", "Compose")) {

    val todoTasks = remember { mutableStateListOf<String>() }

    // 只有当 todoTasks 或 highPriorityKeywords 发生变化时才计算高优先级的任务，而不是在每次重新组合时计算。
    val highPriorityTasks by remember(highPriorityKeywords) {
        derivedStateOf { todoTasks.filter { it.containsWord(highPriorityKeywords) } }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn {
            items(highPriorityTasks) { /* ... */ }
            items(todoTasks) { /* ... */ }
        }
        /* Rest of the UI where users can add elements to the list */
    }
}
```

todoTasks 是 remember ，所以每次 todoTasks 变化都会触发 items() 计算；同理 highPriorityKeywords 也是 remember ，highPriorityKeywords 的变化会触发 derivedStateOf {} 里面的代码最终生成 highPriorityTasks，highPriorityTasks 也是一个 remeber 。

## snapshotFlow

将 Compose 的 State<T> 转换为 Flow

```kotlin
val listState = rememberLazyListState()

LazyColumn(state = listState) {
    // ...
}

LaunchedEffect(listState) {
    snapshotFlow { listState.firstVisibleItemIndex }
        .map { index -> index > 0 }// 当 index > 0 时传递 true, 否则传递 false
        .distinctUntilChanged() //下一个值和上一个值不一样的时候才发送（去重复）
        .filter { it == true }
        .collect {
            MyAnalyticsService.sendScrolledPastFirstItemEvent()
        }
}
```

上面的代码硬是把一个 State <Int> 转成 Flow<Boolean> ，中间去重，过滤，最后收集 Flow

## 组合 —— 布局——绘制

### 组合

组合阶段时，系统会从 compose 函数的参数中查找获取 remember 数据源或者从 compose lambda 中查找获取 remember 数据源

重组器可能会重新重组所有要读取相应数据的可组合函数。如果数据没有发生更改，重组器会跳过重组该组合函数。

当组合完成后，Compose 会进入到布局阶段，根据组合的结果来决定是否需要重新布局，如果组合的结果和之前保持一样，同样也跳过布局阶段；绘制阶段也是一样。

### 布局

如果组合的结果和上次不一样，往下看，进入到布局阶段，也就是需要测量和放置。测量主要通过 `LayoutModifier` 接口的 `MeasureScope.measure` 方法来测量；放置则会运行 `layout` 函数来放置，其中还要处理 Modifier.offset{} 等数据来准确放置。

其中，在每个步骤中对数据(状态)进行读取，也会影响到整个布局阶段，并且甚至可能会影响到绘制阶段（特别是大小和位置发生变化时）。

测量步骤和放置步骤各自具有单独的重启作用域，这意味着，放置步骤中的数据读取不会在读取之前重新调用一次测量步骤；不过，这两个步骤通常是交织在一起的，因此在放置步骤中读取数据，可能会影响属于测量步骤的其他重启作用域。

像下面的代码 如果 offsetX 的数据是在放置步骤中计算偏移量的时候读取的，只要 offsetX 的数据发生变化就会导致重新启动布局。

```kotlin
var offsetX by remember { mutableStateOf(8.dp) }
Text(
    text = "Hello",
    modifier = Modifier.offset {
        // The `offsetX` state is read in the placement step
        // of the layout phase when the offset is calculated.
        // Changes in `offsetX` restart the layout.
        IntOffset(offsetX.roundToPx(), 0)
    }
)
```

### 绘制

绘制期间的状态读取会影响绘制阶段, 常见示例包括 `Canvas()`、`Modifier.drawBehind` 和 `Modifier.drawWithContent`。当数据值发生更改时，Compose 界面只会运行绘制阶段

![](https://developer.android.com/images/jetpack/compose/phases-state-read-draw.svg)

```kotlin
Box {
    val listState = rememberLazyListState()

    Image(
        // Non-optimal implementation!
        Modifier.offset(
            with(LocalDensity.current) { // 不推荐
                // State read of firstVisibleItemScrollOffset in composition
                (listState.firstVisibleItemScrollOffset / 2).toDp()
            }
        )
    )

    LazyColumn(state = listState)
}
```

> 注意: 每个滚动事件都会导致系统重新评估整个可组合项内容，还会导致系统进行测量、布局，最后再进行绘制。即使要显示的**内容**未发生更改，并且只有显示**位置**发生更改时，系统也会在每次滚动时触发 Compose 阶段。因此我们可以优化状态的读取，以便仅重新触发布局阶段![]![avatar](https://developer.android.com/images/jetpack/compose/phases-state-read-draw.svg)

```kotlin
Box {
    val listState = rememberLazyListState()

    Image(
        Modifier.offset { // 优化之后
            // State read of firstVisibleItemScrollOffset in Layout
            IntOffset(x = 0, y = listState.firstVisibleItemScrollOffset / 2)
        }
    )

    LazyColumn(state = listState)
}
```

这两段代码的区别就是，前者把数据传到 () 里面，后者传到 {} ，传到 () 意味着需要重新组合 -> 布局 -> 绘制，传到 {} 只需要重新 布局 + 绘制

有时候重组是不可避免的，但应尽可能减少重组的次数

## 重组循环

```kotlin
Box {
    var imageHeightPx by remember { mutableStateOf(0) }

    Image(
        painter = painterResource(R.drawable.rectangle),
        contentDescription = "I'm above the text",
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                // Don't do this
                imageHeightPx = size.height
            }
    )

    Text(
        text = "I'm below the image",
        modifier = Modifier.padding(
            top = with(LocalDensity.current) { imageHeightPx.toDp() }
        )
    )
}
```

上面的这段代码能很好的解释 Compose 的各个阶段，我们需要知道的一点是 Compose Ui 每次走一遍流程（1帧）都不会发生阶段回退的情况，它一但开始就必须走完 组合+布局+绘制，不能走到布局阶段，先回一次组合阶段再回来。现在开始解释上面的代码：

当 compose1 准备进入组合阶段时，先读取 Box 块内的 remember( imageHeightPx ) 数据源；在进入到组合阶段的时会调用这个数据源并获取里面的值(因为 imageHeightPx 和 Text 有关联)， 我们设置的默认值是 0， 也就是 imageHeightPx = 0；所以设置给 Text 的 panddingTop = 0；完成组合阶段。

compose1 进入到下一个阶段：布局阶段 ；在布局阶段调用了 onSizeChanged, 然后拿到图片的高度，最后修改数据源 imageHeightPx ，由于之前 imageHeightPx = 0，现在不为0了，两次数据的值不一样，就会触发 compose 的重组；但 compose 的这次流程还没走完， compose 会继续先走完这次流程

compose1 进入绘制阶段，此时给 Text 绘制的 paddingTop = 0 ;绘制完成后，由于 compose1 之前在布局阶段又收到了数据源的改变信号。因此需要走准备下一次流程；

compose2 又再次走一遍组合阶段，在组合阶段读取数据源 imageHeightPx 是图片的高度。

compose2 次进入到了布局阶段，在布局阶段又调用了一次 onSizeChanged ,这次发现获取的图片高度和数据源里面的一样，就不用修改数据源了，因此判定当走完这次绘制流程就不用再从头来过。

## 架构分层

从上往下：

- Material
  
  Material Design ，主要针对 Foundation 层中常用 Ui Compose 函数的一些美化

- Fundation
  
   常用 Compose View 函数

- Ui 
  
  工具包Modifier、输入处理程序、自定义布局和 Canvas 绘图等

- Runtime
  
   提供 Compose 运行时的基本组件，例如 [`remember`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#remember(kotlin.Function0))、[`mutableStateOf`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#mutableStateOf(kotlin.Any,androidx.compose.runtime.SnapshotMutationPolicy))、[`@Composable`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Composable) 注解和 [`SideEffect`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#SideEffect(kotlin.Function0))，Compose 树形结构的管理、

## 总结

1. 避免将数据源传给 padding 和 height 等操作符

2. 注意使用 onSizeChanged 和 onGloballyPositioned 等布局操作符

3. 考虑当更新数据源时会发生情况

4. 避免重复

5. 尝试自定义布局来解决上面的问题

6. 单一数据源