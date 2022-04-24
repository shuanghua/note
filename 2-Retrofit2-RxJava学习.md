---
title: Retrofit2 和 RxJava 学习
tags:
  - Android
id: 7
categories:
  - Android
toc: true
comments: true
description: Retrofit2 和 RxJava 学习
date: 2016-3-4
---

# 介绍: #



Retrofit Github地址：[https://github.com/square/retrofit](https://github.com/square/retrofit)

RxJava Github地址：[https://github.com/ReactiveX/RxJava](https://github.com/ReactiveX/RxJava)


>## Retrofit :
> squareup 公司出的一个开源网络请求框架, 好多人说是 Android 下最好用的网络请求库
>## RxJava : 两个角色 



<!-- more -->

> 官方：“一个在 Java VM 上使用可观测的序列来组成异步的、基于事件的程序的库”（反正我没理解懂），可以理解为是一个用于异步操作的库就行了。

> 说白了就是观察者模式， 一个观察者（Observer）， 一个被观察者(Observable)， 两者之间存在一种订阅绑定(Subscribe ) 关系

> Rxjava 还有一个特点是我非常喜欢的， 就是逻辑灰常的“简洁”， 特别是配合 Lambda 一起使用， 代码风格简直帅呆了(●´∀｀●)

> 那么问题来了 Lambda 又是啥玩意儿，如果经常用 AS 的就会发现，打开一个java文件的时候，会发现有个 " -> " 点击它的时候会把代码展开，没错这就是 Lambda 语法，只不过只是个预览化而已，真正用它，AS 还要配置一下才行（后面会讲到）。


> * * *

# 开干
打开 AndroidStudio 开干

## 添加网络权限 ：

	    <uses-permission android:name="android.permission.INTERNET"/>

## 添加依赖 ：
官方建议也添加 rxandroid , 解析用Gson, 再加上出自 jakewharton 大神之手、颜值爆表的 butterknife 注解框架
```gradle
compile 'io.reactivex:rxjava:1.1.1'
compile 'io.reactivex:rxandroid:1.1.0'
compile 'com.squareup.retrofit2:retrofit:2.0.0'
compile 'com.squareup.retrofit2:converter-gson:2.0.0'
compile 'com.squareup.retrofit2:adapter-rxjava:2.0.0'
compile 'com.google.code.gson:gson:2.6.2'
compile 'com.jakewharton:butterknife:7.0.1'
```
## 布局文件 ：
一个文本展示下载的数据，一个按钮触发下载
```xml
<?xml version="1.0" encoding="utf-8"?>
	<RelativeLayout
	    xmlns:android="http://schemas.android.com/apk/res/android"
	    xmlns:tools="http://schemas.android.com/tools"
	    android:layout_width="match_parent"
	    android:layout_height="match_parent"
	    android:paddingBottom="@dimen/activity_vertical_margin"
	    android:paddingLeft="@dimen/activity_horizontal_margin"
	    android:paddingRight="@dimen/activity_horizontal_margin"
	    android:paddingTop="@dimen/activity_vertical_margin"
	    tools:context="com.shuanghua.rxre.MainActivity">
	
		    <TextView
		        android:id="@+id/text"
		        android:layout_width="wrap_content"
		        android:layout_height="wrap_content"
		        android:text="Hello World!"/>
		
		    <Button
		        android:id="@+id/button"
		        android:layout_width="match_parent"
		        android:layout_height="wrap_content"
		        android:layout_alignParentBottom="true"
		        android:layout_centerHorizontal="true"
		        android:text="获取数据"/>
	</RelativeLayout>
```
## Java Bean : BaiKe.java (太长就不贴出来了)
下载数据的接口 ：[http://m2.qiushibaike.com/article/list/suggest?page=1](http://m2.qiushibaike.com/article/list/suggest?page=1 "http://m2.qiushibaike.com/article/list/suggest?page=1")


## 创建一个服务接口 ApiService.java
```java
public interface ApiService {

	//贴出接口链接，方便对照，用法都是 Retrofit2 的用法
	//http://m2.qiushibaike.com/article/list/suggest?page=1
	
	@GET("suggest")
	Observable<BaiKe> getBaKeData(@Query("page") int page);//如只用Retrofit 返回 Call, RxJava 返回 Observable
}
```
## MainActivity.java

请自行 Butterknife 实例化控件

下面主要讲 Rxjava 
```java
	private void getBaiKeData() {
        String baiKeUrl = "http://m2.qiushibaike.com/article/list/";

        //第一 retrofit 对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baiKeUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//增加对 rxjava 的支持
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //第二 Service 对象
        ApiService baiKeService = retrofit.create(ApiService.class);

        //第三 调用Service里获取数据的方法
        Observable<BaiKe> baKeData = baiKeService.getBaKeData(1);

        //第四 请求并处理数据
        baiKeService.getBaKeData(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaiKe>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaiKe baiKe) {
                        mText.setText(baiKe.getItems().get(7).getContent());
                    }
                });
    }
```

从上往下都是构建模式，是不是帅呆了 (•‾̑⌣‾̑•)✧˖°

是不是发现逻辑简洁了, 但我觉得还是不够帅。。

下面我们来封装它，不然我在别的页面要展示一样的数据，难道都要写这么多的代码， 面向对象是啥？？ 消除重复代码嘛， 要我写这么多的代码，这是绝对不能忍的。!


封装我们的代码，把 MainActivity 里下次再请求，要重复写一遍的代码抽到单例类里面（封装大多数是看需求而定的）。
# 封装
## 核心 ：经观察只要 BaseUrl 不变，全程只需要一个 Service 对象就够了

所以， 单例少不了，还有不是很标准的工厂

两个类，一个负责生产Service 对象， 一个负责获取Service 对象

## ServiceManager （负责生产Service 对象）

```java
/**
 * ApiService管理器，方便有新的基地址加入
 * Created by ShuangHua
 */
public class ServiceManager {

    //只要 BaseUrl 不变，全程只需要一个 Service 对象就够了

    public static final String BASE_URL_BAIKE = "http://m2.qiushibaike.com/article/list/";
    private static final int DEFAULT_TIMEOUT = 5;
    private final ApiService mService;

    /**
     * Service 对象生成机器
     */
    public ServiceManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_BAIKE)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(ApiService.class);
    }

    /**
     * 生成后让工厂送出
     */
    public ApiService getApiService() {
        return mService;
    }
}
```

## ServiceFactory.java （一个负责获取Service 对象）

```java
**
 * 负责获取 mService 对象
 * 具体的生成交给机器（管理器）
 * Created by ShuangHua
 */
public class ServiceFactory {
    public static final Object monitor = new Object();
    public static final int page = 1;
    static ApiService mService = null;

    public static ApiService getService() {
        synchronized (monitor) {
            if (mService == null) {
                //叫机器生产，坐等产品
                mService = new ServiceManager().getApiService();
            }
            return mService;//出厂销售。。。
        }
    }
}
```

	
## MainActivity 里的调用

```java
public void getData() {
        mService.getBaKeData(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<BaiKe>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaiKe baiKe) {
                        mText.setText(baiKe.getItems().get(7).getContent());
                    }
                });
    }
```

***
***

&nbsp;
# Lambda jdk8+
为了让上面 MainActivity 处的代码变得更帅，我加入 Lambda jdk8+

## 配置
第一步 ：在项目级的 build.gradle 中加入:

<pre>classpath 'me.tatarka:gradle-retrolambda:3.3.0-beta4'</pre>


这样：(添加完第一步记得 Sync Gradle)
```gradle
		buildscript {
	    repositories {
	        jcenter()
	    }
	    dependencies {
	        classpath 'com.android.tools.build:gradle:2.1.0-alpha1'


			// 在这里
	        classpath 'me.tatarka:gradle-retrolambda:3.3.0-beta4'//======>>第1步


			// NOTE: Do not place your application dependencies here; they belong
	        // in the individual module build.gradle files
	    }
	}
```
第二步 ：在Module级的 build.gradle 中加入
```gradle
	apply plugin: 'com.android.application'
	apply plugin: 'me.tatarka.retrolambda'//====================>>第2步
	android {
	    compileSdkVersion 23
	    buildToolsVersion "23.0.2"
	
	    defaultConfig {
	        applicationId "com.shuanghua.rxre"
	        minSdkVersion 15
	        targetSdkVersion 23
	        versionCode 1
	        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    compileOptions {//==========================================>>第3步
	        sourceCompatibility JavaVersion.VERSION_1_8
	        targetCompatibility JavaVersion.VERSION_1_8
    	}
	}

	dependencies {
	    compile fileTree(include: ['*.jar'], dir: 'libs')
	    testCompile 'junit:junit:4.12'
	    compile 'com.android.support:appcompat-v7:23.1.1'
	    compile 'io.reactivex:rxandroid:1.1.0'
	    compile 'com.squareup.retrofit2:retrofit:2.0.0-beta4'
	    compile 'com.squareup.retrofit2:converter-gson:2.0.0-beta4'
	    compile 'com.squareup.retrofit2:adapter-rxjava:2.0.0-beta4'
	    compile 'com.squareup.okhttp:okhttp:2.7.4'
	    compile 'com.squareup.picasso:picasso:2.5.2'
	}
```
Sync Gradle 完成之后。

我们来用下 Lambda 

选中 subscribe() 括号里的代码 Ctrl + Alt + Space 回车

![](http://7xrysc.com1.z0.glb.clouddn.com/rxre.jpg)

## 使用
最后代码变这样 ：

![](http://f1.diyitui.com/aa/6e/16/ed/d1/6a/6f/e4/80/f9/07/43/d0/5c/dd/4a.jpg)


```java
 public void getData() {
        mService.getBaKeData(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baiKe -> 请在这里处理你的数据);
    }
```

```java
mService.getBaKeData(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baiKe -> mText.setText(baiKe.getItems().get(7).getContent()));
```


再变
```java
mService.getBaKeData(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baiKe -> mText.setText(baiKe
                        .getItems()
                        .get(7)
                        .getContent()));
```

因为设置了 observeOn(AndroidSchedulers.mainThread()) 所以最后处理数据是在 UI 线程里的


# 结尾 
以上就是 Retrofit2 + Rxjava的简单用法
> Rxjava 还有很多的操作符， 比如 from()、 just()、flatMap()、filter()、map()、 我也是正在学。