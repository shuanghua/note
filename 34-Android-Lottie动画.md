---
toc: false
comments: true
title: Lottie 在 Android 上快速实现复杂的动画效果
description: Lottie 是一个第三方的动画开源库，它利用 Adobe After Effects （后面统一称为AE）和 Adobe After Effects 插件 Bodymovin 来解析 AE 动画，最终将 AE 动画解析成为 json 文件或 xml 类型的 AVD(AnimatedVectorDrawable) 文件。 最后把 json 文件或 xml 文件导入到 AS 中，利用 Lottie 代码把 json  动画显示到 Android 设备界面上。
tags:
  - Android
id: 34
categories:
  - Android
date: 2018-5-20
---


Lottie 是一个第三方的动画开源库，它利用 Adobe After Effects （后面统一称为AE）和 Adobe After Effects 插件 Bodymovin 来解析 AE 动画，最终将 AE 动画解析成为 json 文件或 xml 类型的 AVD(AnimatedVectorDrawable) 文件。 最后把 json 文件或 xml 文件导入到 AS 中，利用 Lottie 代码把 json  动画显示到 Android 设备界面上。


<!-- more -->

Lottie Github :[https://github.com/airbnb/lottie-android](https://github.com/airbnb/lottie-android)

我们都知道，在 Android 上用纯代码来绘制动画是一件非常麻烦的事情，各种参数的计算让开发头疼不已，而有了 Lottie ,动画参数计算和效果绘制交给UI设计的妹子就好了。

用了 Lottie 之后咱们就天天看设计人员忙就行了，因为设计绘制动画的事已经不归你管了[滑稽]，当然有时候也得自己设计动画（前提是能够熟练使用AE），比如个人项目或者项目压根就没有设计人员[笑哭]，再或者设计是个妹子，你不希望她劳累[阴险]

以下只是关于 Lottie 使用，不涉及如何使用 AE 来设计好看的动画。

# 安装 Adobe After Effects 和 Bodymovin 插件
- Adobe After Effects 下载：“都是干技术活的，自行搜索破解版”，或“自行搜索官网支持正版”
- Bodymovin 插件下载地址：[https://www.adobeexchange.com/creativecloud.details.12557.html](https://www.adobeexchange.com/creativecloud.details.12557.html)

- 如果是破解版，安装 Bodymovin 建议通过 ZXPInstaller 这个软件来安装。因为破解版不支持直接安装插件。

- ZXPInstall : [http://zxpinstaller.com/](http://zxpinstaller.com/)

# 使用流程
比如需要给 App 的启动引导页面设计一个好看的过度动画

- 1.让设计美工绘制

- 2.利用 Bodymovin 插件导出 Glyphs(json 文件)或者 AVD(xml 文件)
  > 窗口 -> 扩展 -> Bodymovin 导出
  
![](http://7xrysc.com1.z0.glb.clouddn.com/%E5%AF%BC%E5%87%BA.png)
![](http://7xrysc.com1.z0.glb.clouddn.com/%E5%AF%BC%E5%87%BA1.png)

- 3.将 json 或 xml 文件导入到 AndroidStudio 中
  > 推荐 AndroidStudio 3.0 + ,同时将 buildToolsVersion 升级到27.0.1+(此版本修复了 原先 xml 嵌套资源文件的 Bug，如果使用 json 可以忽略)

- 4.添加 Lottie Gradle 依赖

```gradle
dependencies {
  implementation 'com.airbnb.android:lottie:2.5.4'
}
```

- 布局文件中使用

这里我把 json 文件放到 raw 目录下

```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

        <com.airbnb.lottie.LottieAnimationView
            android:id="@+id/animation_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent"
            app:lottie_autoPlay="true""
            app:lottie_loop="true"
            app:lottie_rawRes="@raw/data" />

</android.support.constraint.ConstraintLayout>
```

- 然后运行就好了


整个过程非常的简单，麻烦的事情都托付给设计同事了，哈哈。如果你也像设计出好看的动画，学会 AE 就好了。


# Lottie 优点
- 快速动画开发（高难度动画情况）
- 跨平台
- 服务器后台控制调整动画
- 进度控制，像利用触摸滑动来控制动画执行的进度
- 最低 Android API 14 （AVD 需要 API24+）
- 代码开源


# Lottie 缺点
- **json 加载性能差**,**通过 json 加载**的形式来绘制动画，性能是非常差的，所以推荐在启动引导页面上使用，或不需要和用户交互时使用，从而避免用户感受到页面卡顿；这个就要看你是否愿意牺牲性能来换取效果。

> 而通过 AVD(AnimatedVectorDrawable)的xml 的加载方式性能会很好，但 AnimatedVectorDrawable 需要在 API24+ 上才能使用。

- **不好调试**，一旦导入到 AS 后，就很难调试动画了，除非你吃透源码看懂 json 里面的数据参数。在这方面，AnimatedVectorDrawable 可读性就好很多。

# Lottie 使用注意点
- AVD 只支持 矢量文件资源，不支持普通资源，例如把 png 图片放到 AE 里面去添加动画，然后导出成 AVD（xml）,最后引用该 xml 是会报错的。所以使用 AVD 导出，请让 Designer 画吧。 

- json 支持应用 普通资源文件，导出后会有一个 image 的资源文件夹，请复制到 AS 中，然后需要在代码中或布局xml控件中指定该文件夹

```xml
	<com.airbnb.lottie.LottieAnimationView
		//这里我把 image 文件加放到了 assets 目录下
        app:lottie_imageAssetsFolder="images"/>
```

```kotlin
	//kotlin 代码
    animation_view.apply {
        imageAssetsFolder = "images/"  //指定资源目录
        setAnimation("data.json")
        loop(true)
        playAnimation()
    }
```

