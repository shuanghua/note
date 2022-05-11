## 一：定义每个页面
```kotlin
internal sealed class Screen(val route: String) {
    object Weather : Screen("weather")
    object Favorite : Screen("favorite")
    object More : Screen("more")
}
```



## 二：定义页面集合
```kotlin
val items = listOf(Screen.Favorite, Screen.Weather, Screen.More)
```
通常只在类似于 BottomNavigationView 需要定义一组 Item 时才需要



## 三：定义导航关系图表
```kotlin
@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Weather.route) {
        composable(Screen.Favorite.route) { FavoriteScreen() }
        composable(Screen.Weather.route) { WeatherScreen() }
        composable(Screen.More.route) { MoreScreen() }
    }
}
```



## 四：每个页面的具体跳转目标
```kotlin
navController.navigate(MoreScreen.route) //这里举例i跳转到 MoreScreen 页面 , 此代码通常放在 onClick 回调中执行。
```



## 五: 调用 BottomNavigation （compose 自带组件）
```kotlin
val navController = rememberNavController()
Scaffold(
    bottomBar = {
        BottomNavigation {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            items.forEach { screen -> //遍历页面集合
                BottomNavigationItem( //循环顺序添加 BottomNavigationItem
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true, //设置选中状态
                    onClick = {// 点击时，每个 item 的跳转目标
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {saveState = true}
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
) {
    NavigationGraph(navController) //调用导航图表
}
```

## 传递和接收参数
```kotlin
NavHost(startDestination = "profile/{userId}") {
    composable("profile/{userId}") //传入
    composable("profile/{userId}", arguments = listOf(navArgumen("userId"{ type = NavType.StringType })) //接收
}
```



## 嵌套导航
```kotlin
NavHost(navController, startDestination = "home") {
    navigation(startDestination = "username", route = "login") {
        composable("username") { ... }
        composable("password") { ... }
        composable("registration") { ... }
    }
}
```










