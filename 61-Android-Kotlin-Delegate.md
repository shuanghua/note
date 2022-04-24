---
toc: false
comments: false
title: Kotlin Delegate 代码笔记
description: Kotlin Delegate 代码笔记
tags:
  - Kotlin
id: 61
categories:
  - Kotlin
date: 2019-8-17
---

Kotlin Delegate 代码笔记
<!-- more -->

#### 类代理
代理也叫委托,代理模式能很好的替代普通继承


```kotlin
interface ThemeActivityDelegate {
	val currentTheme: Theme
}
```


```kotlin
class ThemeActiviteDelegateImpl(
	private val observeThemeUseCase: ObserveThemeModeUseCase,
    private val getThemeUseCase: GetThemeUseCase
) : ThemeActivityDelegate {
	init {
        observeThemeUseCase.execute(Unit)
    }

    override val theme: LiveData<Theme> by lazy(NONE) {
        observeThemeUseCase.observe().map {
            if (it is Success) it.data else Theme.SYSTEM
        }
    }

    override val currentTheme: Theme
        get() = getThemeUseCase.executeNow(Unit).let {
            if (it is Success) it.data else Theme.SYSTEM
        }
}
```

```kotlin
class MainActivityViewModel(
	themedActivityDelegate: ThemedActivityDelegate
) :ViewModel(),ThemeActivityDelegate by themedActivityDelegate {

}
```



```kotlin
class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //使用
        val theme = viewModel.currentTheme
    }
}
```

> 看着好像和普通的继承没啥区别, 但好处就是我们让我们的 MainActivity 变得干净了,它只需要一个 ViewModel 就够了,并且 ViewModel 自身只需实现顶级的接口,不需继承具体的实现类 ThemeActiviteDelegateImpl(如果继承实现类,还需要把该类设置为 open,安全性也降低了)

> 总结: 写一个接口A, 写一个实现类实现这接口A, 在使用类中传入接口 A:a 同时实现接口A by a


#### 属性代理
```kotlin
interface PreferenceStorage {
    var selectedTheme: String?
    var observableSelectedTheme: LiveData<String>
}
```

```kotlin
class SharedPreferenceStorage (context: Context) : PreferenceStorage {
    companion object {
        const val PREF_DARK_MODE_ENABLED = "pref_dark_mode"
        const val PREFS_NAME = "prefs"
    }

    private val observableSelectedThemeResult = MutableLiveData<String>()

    private val prefs: Lazy<SharedPreferences> = lazy {
        context.getSharedPreferences(
            PREFS_NAME, MODE_PRIVATE
        ).apply {
            registerOnSharedPreferenceChangeListener(changeListener)
        }
    }

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            // 当数据改变时，将改变的数据放到一个 LiveData 中
            PREF_DARK_MODE_ENABLED -> observableSelectedThemeResult.value = selectedTheme
        }

    }

    /**
     * Kotlin 属性代理
     */
    override var selectedTheme: String? by StringPreference(
        prefs,
        PREF_DARK_MODE_ENABLED,
        Theme.SYSTEM.storageKey
    )

    override var observableSelectedTheme: LiveData<String>
        get() {
            observableSelectedThemeResult.value = selectedTheme
            return observableSelectedThemeResult
        }
        set(_) = throw  IllegalAccessException("不能对此实例进行赋值！")
}
```


#### 属性代理例子
- 构造像下面这样得代码：
```kotlin
override var selectedTheme by StringPreference(
        prefs, PREF_DARK_MODE_ENABLED, Theme.SYSTEM.storageKey
    )
```

- 或:
```kotlin
private val viewModel: MainViewModel by viewModels { viewModelFactory }
```

- 拿 selectedTheme 举例子，首先定义一个 interface
```kotlin
interface PreferenceStorage(var selectedTheme: String?)
```


- 定义代理类，这里使用了标准库 ReadWriteProperty 来实现（重点）
```kotlin
class StringPreference(
    private val preferences: Lazy<SharedPreferences>,
    private val name: String,
    private val defaultValue: String?
) : ReadWriteProperty<Any, String?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return preferences.value.getString(name, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        preferences.value.edit { putString(name, value) }
    }
}
```


- 实现上面的接口，实现 selectedTheme 对象赋值
```kotlin
class SharedPreferenceStorage(context: Context) : PreferenceStorage {
	 context.applicationContext.getSharedPreferences(
            PREFS_NAME, MODE_PRIVATE
        ).apply {
            registerOnSharedPreferenceChangeListener(changeListener)
        }
    
    private val changeListener = OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PREF_DARK_MODE_ENABLED -> observableSelectedThemeResult.value = selectedTheme
        }
    }
	//上面只是初始化 SharedPreference 

 	override var selectedTheme by StringPreference(
        	prefs, PREF_DARK_MODE_ENABLED, Theme.SYSTEM.storageKey
    )
}
```

如果要实现像下面这样：
```kotlin
private val viewModel: MainViewModel by viewModels { viewModelFactory }
```

去掉上面的接口，StringPreference 修改成 viewModels，构造函数定义成 lambda 即可

