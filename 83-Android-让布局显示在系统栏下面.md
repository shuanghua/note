### 1 . style 或者 theme 中设置

状态栏透明 + 取消默认ActionBar + Window标题

```xml
<item name="android:statusBarColor" tools:targetApi="l">@android:color/transparent</item>
<item name="windowActionBar">false</item>
<item name="windowNoTitle">true</item>
```



### 2.利用设置了WindowInsets的布局

比如 CoordinatorLayout 或 AppBarLayout 或 DrawerLayout 等内部监听设置了 WindowInsets systembar 的布局, 然后在该布局中设置

```xml
android:fitsSystemWindows="true"
```



### 3.自定义布局监听WindowInsets

```kotlin
if (ViewCompat.getFitsSystemWindows(this)) {
    setOnApplyWindowInsetsListener { view, insets -> setWindowInsets(insets, view) }
    systemUiVisibility = (SYSTEM_UI_FLAG_LAYOUT_STABLE or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
}
```

```kotlin
private fun setWindowInsets(insets: WindowInsets, view: View): WindowInsets {
    val drawerContainer = view as SlideDrawerContainer
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val systemBar = insets.getInsets(WindowInsets.Type.systemBars())
        drawerContainer.setChildInsets(insets, systemBar.bottom > 0)
        WindowInsets.CONSUMED
    } else {
        drawerContainer.setChildInsets(insets, insets.systemWindowInsetTop > 0)
        insets.consumeSystemWindowInsets()
    }
}
```

然后在 xml 中设置,当然在代码中也可以设置,也可以集成到自定义 ViewGroup 中

```xml
android:fitsSystemWindows="true"
```


