## 自定义 Function Layout
默认的 "可测量元素" 根据 "约束" 来创建 "可放置的元素对象"
可放置的元素其实就是已经确定大小的元素

- 获取测量元素以及对应约束参数 -> 放置
Function 调用的是 @Compose Layout()  ->  MeasureScope.layout()
Modifier 调用的是 Modifier.layout()  ->  MeasureScope.layout()


### 写一个普通的 Composable 函数
```kotlin
@Composable
fun CustomColumLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
}
```


### 调用 Layout 函数, Layout 提供了测量元素以及对应约束参数
> 如果是自定义 modifier , 则应该使用小写的 layout 来获取测量元素以及对应约束参数
```kotlin
@Composable
fun CustomColumLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

    }
}
```


### 调用小 layout 函数进行放置元素 (这里的 layout() 是 MeasureScope 接口下的)
```kotlin
@Composable
fun CustomColumLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->

        // 根据约束开始测量, 并把已测量的元素放到一个新集合中(已确定大小的元素)
        val placeables = measurables.map {
            it.measure(constraints)
        }

        // 创建一个布局来放置已测量的元素, 布局的宽高默认最大
        // 这里仿写 Colum 布局, 所以放置的逻辑主要是考虑 y 坐标的位置
        layout(constraints.maxWidth, constraints.maxHeight) {
            var yPosition = 0
            // 放置元素
            placeables.forEach { placeable ->
                placeable.placeRelative(x = 0, y = yPosition)
                yPosition += placeable.height
            }
        }
    }
}
```


## 自定义 Modifier (需要调用小写的 layout() 来获取可测量元素和约束)
因为 modifier 一般都设置给单个元素, 所以 Modifier 的 layout 只提供一个可测量元素
```kotlin
fun Modifier.expandModifier(
    firstBaselineToTop: Dp
    ) = this.layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val firstBaseline = placeable[FirstBaseline]

    val placeableY = firstBaselineToTop.roundToPx() - firstBaseline
    val height = placeable.height + placeableY
    
    layout(placeable.width, height) {
        placeable.placeRelative(0, placeableY)
    }
}
```




## 标准的自定义 Modifier 
参考 Modifier.padding
```kotlin
fun Modifier.custom(all: Dp) = this.then(
    CustomModifier(
        inspectorInfo = debugInspectorInfo {
            name = "padding"
            value = all
        }
    )
)
```

### 实现 LayoutModifier 接口 和 InspectorValueInfo 抽象类
```kotlin
class CustomModifier(    
    inspectorInfo: InspectorInfo.() -> Unit
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {
    
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        //...
        layout(width, height) {
            placeable.placeRelative() // 放置
        }
    }

    override fun hashCode(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        return true
    }
}
```























