# 单模块
### NavHost
```kotlin
@Composable
fun AppNavHost(
	navController: NavHostController,
	modifier: Modifier = Modifier
) {
    NavHost(
		navController = navController,
		startDestination = Home, // 告诉 NavBottomBar 进入应用后要打开的哪个item navigation route ,
		modifier = modifier
	) {
        homeScreen(
            navController = navController,
            nestedGraphs = {
                otherScreen1(navController = navController)
                otherScreen2(navController = navController)
            }
        )

        favoritiesScreen(
            navController = navController,
            nestedGraphs = {
                otherScreen3(navController = navController)
                otherScreen4(navController = navController)
            }
        )

        moreScreen(
            navController = navController,
            nestedGraphs = {
                otherScreen5(navController = navController)
                otherScreen6(navController = navController)
            }
        )
    }
}
```


### HomeScreen 
- 参数定义
```kotlin
@Serializable
data object Home
```


- 暴露调用（暴露给其它页面调用）
```kotlin
fun NavHostController.navigateToHome(home: Home) {
    navigate(route = home) {
        launchSingleTop = true
        // popUpTo(Home) { inclusive = false }
    }
}

```


- 启动入口（提取其它页面传递过来的数据）
```kotlin
fun NavGraphBuilder.homeScreen(
    navController: NavHostController,
    nestedGraphs: () -> Unit
) {
    navigation<Home>(
        startDestination = Home
    ) {
        composable(route = Home) {
            HomeScreen(
                openProvinceScreen = openProvinceScreen,
                openFavoriteWeatherScreen = openFavoriteWeatherScreen
            )
        }
        nestedGraphs()
    }

}
```



```kotlin

```



```kotlin

```




























