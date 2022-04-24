---
toc: false
comments: true
title: Dagger2-Android 官方例子笔记
description: 本笔记不涉及 Dagger2 的详细使用，比如某某个注解的作用，含义都不会介绍到，本笔记仅介绍 Dagger2-Android 在 Android 项目的使用,内容都是基于 Dagger2,如果对 Dagger2 还不了解，请先去了解 Dagger2 的使用。
tags:
  - Android
id: 41
categories:
  - Android
date: 2018-7-17
---


本笔记不涉及 Dagger2 的详细使用，比如某某个注解的作用，含义都不会介绍到，本笔记仅介绍 Dagger2-Android 在 Android 项目的使用,内容都是基于 Dagger2,如果对 Dagger2 还不了解，请先去了解 Dagger2 的使用。

<!-- more -->


# Dagger2-Android
想在 Android 项目中使用 Dagger2-Android 这个模块，首先要了解自己项目的结构，结合自己项目的结构来使用 Dagger2-Android 是最好的。使用 Dagger2-Android 一般有两种方式：

## 使用方式1 (个人推荐)
这种方式是 Google IO/2018-Android App 代码中的方式。 例子参考于:[ Google IO/2018-Android App][1]

### MyApplication 继承 DaggerApplication{}
这种方式中，DaggerApplication 其实就是在方式 2 上做了封装

- 1 Application
```kotlin
class MyApplication : DaggerApplication{
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
```

- 2 AppComponent
```kotlin
@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            AppModule::class,
            ActivityBindingModule::class,
            SharedModule::class,
            ViewModelModule::class
        ]
)
interface AppComponent : AndroidInjector< MyApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder< MyApplication>()
}
```
其中 AndroidInjectionModule::class 是 Dagger2-Android 中自带的，他是一个 Module，说明他专门生产实例（提供依赖实例），它专门生产 Android 四大组件的实例；在 AppComponent 中引入它，是为了更方便在四大组件中使用别的相关实例。

ActivityBindingModule::class 是自己写的，专门用于生产和管理具体的 Activity 实例; 假设我们的 Activity 名字叫 MainActivity，那么 ActivityBindingModule 中必须包含一个返回 MainActivity 实例的抽象函数（除非你不想在这个 Activity 使用 Dagger 注入），同时为了解耦，我们在新建一个 MainActivityModule 来专门为 MainActivity 生产实例， 然后就像下面这个样子:
```kotlin
@Module
abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(
                modules = [
                    MainActivityModule::class
		]
    )
    abstract fun mainActivity(): MainActivity

    //以下代码在本文中无关紧要
    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun LauncherActivity(): LauncherActivity
}
```

再假设，我们的 MainActivity 中有很多个 Fragment (WeatherFragment、FavoriteFragment、CityFragment...)，我们就必须要为每个 Fragment 都新建一个 Module 来专门提供该 Fragment 的实例。然后 ActivityBindingModule 应该是这个样子:
```kotlin
@Module
abstract class ActivityBindingModule {
    @ActivityScoped
    @ContributesAndroidInjector(
            modules = [
		MainActivityModule::class,
                WeatherModule::class,
                FavoriteModule::class,
                ProvinceModule::class,
                CityModule::class,
                StationAreaModule::class,
                StationModule::class
            ]
    )
    abstract fun mainActivity(): MainActivity

    @ActivityScoped
    @ContributesAndroidInjector
    abstract fun LauncherActivity(): LauncherActivity
}
```

只要该 Activity 下存在 Fragment ，就需要在 ContributesAndroidInjector 中引入一个的 Module, 并且这个 Module 必须是抽象的，其中还有必须有一个返回该 Fragment 实例的抽象函数 （否则编译出错）。比如像 WeatherModule:
```kotlin
@Module
abstract class WeatherModule {
    @FragmentScoped
    @ContributesAndroidInjector
    abstract fun contributeWeatherFragment(): WeatherFragment//必须
}
```

当 WeatherFragment 中需要注入实例时，为了解耦，我们还需新建一个 Module 来专门为 WeatherFragment 生产实例。
```kotlin
abstract class WeatherModule {
    @FragmentScoped
    @ContributesAndroidInjector(
                modules = [
                    WeatherFragmentModule::class
		]
    )
    abstract fun contributeWeatherFragment(): WeatherFragment//必须
}

@Module
class WeatherFragmentModule {
    @Provides
    fun provideNetworkApiClient(): OkHttpClient{
        return OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS)
                    .writeTimeout(TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
                    .readTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS)
                    .build()
    }
}
```

- Activity Fragment
最后在需要注入依赖的 Activity 继承 DaggerAppCompatActivity / DaggerActivity
最后在需要注入依赖的 Fragment 继承 DaggerFragment


## 使用方式2
这种方式是 Android Architecture Components samples - GithubBrowserSample 代码中的方式。例子参考于 :[Android Architecture Components samples - GithubBrowserSample][2]

### MyApplication 继承 Application 实现 HasActivityInjector{}
- 1 Application
```kotlin
class MyApplication : Application(), HasActivityInjector {
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        AppInjector.init(this)
    }

    override fun activityInjector() = dispatchingAndroidInjector
}
```

- 2 AppInjector
```kotlin
object AppInjector {
    fun init(application: MyApplication) {
        DaggerAppComponent.builder().application(application)
            .build().inject(application)
        application
            .registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    handleActivity(activity)
                }

                override fun onActivityStarted(activity: Activity) {

                }

                override fun onActivityResumed(activity: Activity) {

                }

                override fun onActivityPaused(activity: Activity) {

                }

                override fun onActivityStopped(activity: Activity) {

                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {

                }

                override fun onActivityDestroyed(activity: Activity) {

                }
            })
    }

    private fun handleActivity(activity: Activity) {
        if (activity is HasSupportFragmentInjector) {//Activity 注入时，注意这里
            AndroidInjection.inject(activity)
        }
        if (activity is FragmentActivity) {
            activity.supportFragmentManager
                .registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            savedInstanceState: Bundle?
                        ) {
                            if (f is Injectable) {//在 Fragment 注入时，注意这里有个接口
                                AndroidSupportInjection.inject(f)
                            }
                        }
                    }, true
                )
        }
    }
}
interface Injectable//定义接口
```

- 3 AppComponent
```kotlin
@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        AppModule::class,
        MainActivityModule::class]
)
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(application: MyApplication)
}
```

- 4 MainActivityModule (假设 MainActivity 下存在 RepoFragment、UserFragment 和 SearchFragment 三个 Fragment)
```kotlin
@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])//因为存在 Fragment，所以必需新建一个 FragmentBuildersModule
    abstract fun contributeMainActivity(): MainActivity
}
```

- 5 FragmentBuildersModule
```kotlin
@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeRepoFragment(): RepoFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFragment(): UserFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment
}
```

- 6 Activity Fragment
Activity 继承 AppCompatActivity() 实现 HasSupportFragmentInjector
Fragment 继承 Fragment() 实现 Injectable


# 总结
以上两种方式的好处都是避免了在 Activity 或 Fragment 中初始化 Dagger；根据 Dagger 的设计使用原则，在 Android 中不应该让四大组件知道注入的实例从哪里而来。就像 Dagger 的名字一样“匕首”,突然的从身后刺入，让敌人毫无察觉。

Android Architecture Components 的 MVVM 很依赖 Dagger。

Dagger 对刚上手的朋友还是有点难度的，熟能生巧，熟巧之后，每天出门都想带一把“匕首”!


  [1]: https://github.com/google/iosched
  [2]: https://github.com/googlesamples/android-architecture-components/tree/master/GithubBrowserSample
