---
toc: false
comments: true
title: Android Style 笔记
description: 针对学习 MaterialComponents 的样式笔记
tags:
  - Android
id: 37
categories:
  - Android
date: 2018-6-12
---


没有 android 开头的 item 为 AndroidCompat 支持库中的属性;
有 android 开头的 item, Framework 框架属性，就比如说 TextView 的 layout_width 这个属性，它前面总是 android: ，系统版本不一样，属性就会出现差异。

<!-- more -->


像下面这样的，一定要区别有 andorid 开头和没有 android 开头：
```xml
    <style name="Theme.Shrine" parent="Theme.MaterialComponents.Light.NoActionBar">
       
		<!-- 重写父类样式的属性 -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>

		<!-- 重写 framework 中的 attr 中 textColorPrimary 的值 ，后面引用该属性的时候，都是该值-->
        <item name="android:textColorPrimary">#fb0c0c</item>
    </style>
```

> 还有上面代码 colorPrimary，colorPrimaryDark 这两个属性要注意，它默认情况下决定了 statusBar 和 toolbar 的颜色.但它的字面意思是主要的颜色，这是间接的设置颜色；
> >除此之外，我们还可以通过直接继承系统默认 ActionBar Stley 来修改 statusBar 和 actionbar 样式属性，从而更加直接达到修改颜色的目的，在这种方式下，还能连 background、 theme、 titleTextAppearance 等属性也一同修改。


> 引用风格样式的方式以下常见三种：
1. 在布局文件的xml中通过 style = "@style/" 方式引用
2. 在布局文件的xml中通过 theme = "@style" （前提是该控件存在them的）
3. java 代码中设置

# 一些样式标签的笔记：

- Toolbar 白底黑字黑色图标
```xml
<style name="MyToolbar" parent="Widget.AppCompat.Toolbar">
    <item name="android:theme">@style/ThemeOverlay.AppCompat.ActionBar</item>
    <item name="popupTheme">@style/ThemeOverlay.AppCompat.Light</item>
    <item name="android:background">?attr/colorPrimary</item>//白色
</style>
```


- Toolbar 黑底白字白色图标
```
<style name="Widget.Shrine.Toolbar" parent="Widget.AppCompat.Toolbar">
   	<item name="android:theme">@style/ThemeOverlay.AppCompat.Dark.ActionBar</item>
   	<item name="popupTheme">@style/ThemeOverlay.AppCompat.Light</item>
    <item name="android:background">?attr/colorPrimary</item>//黑色
</style>
```


- 状态栏中使用黑色图标-应用层
```xml
<item name="android:windowLightStatusBar" tools:targetApi="m">true</item>
```


- 控件被选中激活时的颜色-应用层
```xml
<item name="colorControlActivated">#000</item>
```


- Button Style 1
![](http://7xrysc.com1.z0.glb.clouddn.com/button_login_next.png)
- 
```xml
<!-- Login Next Button -->
<style name="Widget.Shrine.Button" parent="Widget.MaterialComponents.Button">
    <item name="android:textColor">@color/toolbarIconColor</item>
    <item name="backgroundTint">@color/colorAccent</item>
    <item name="android:stateListAnimator" tools:ignore="NewApi">
        @animator/shr_next_button_state_list_anim
    </item>
</style>
```
[shr_next_button_state_list_anim](https://gist.github.com/cf96e16ad0005e9cb3ae0f708c4d35c6.git)


- Button Style 2
![](http://7xrysc.com1.z0.glb.clouddn.com/button_login_cancel.png)
- 
```xml
<!-- Login Cancel Button -->
<style name="Widget.Shrine.Button.TextButton" parent="Widget.MaterialComponents.Button.TextButton">
    <item name="android:textColor">@color/colorAccent</item>
</style>
```


- 状态栏上层颜色，半透明覆盖在 DrawerLayout 上层
```xml
<item name="android:statusBarColor">#1e000000</item>//透明度
```


- DrawerLayout 开关图标在 Toolbar 上的颜色-应用层
```xml
<item name="drawerArrowStyle">@style/DrawerArrowStyle</item>//放到应用层Theme
<style name="DrawerArrowStyle" parent="Widget.AppCompat.DrawerArrowToggle">
        <item name="color">?android:attr/textColorPrimary</item>
</style>
```


- 重写 TextInputLayout 例子 - 控件层
```xml
 <!-- TextInputLayout 文本字段的样式 -->
<style name="Widget.Shrine.TextInputLayout" parent="Widget.MaterialComponents.TextInputLayout.OutlineBox">
      <item name="hintTextAppearance">@style/TextAppearance.Shrine.TextInputLayout.HintText</item>
      <item name="android:paddingBottom">8dp</item>
</style>
```


- 重写一些字体例子-控件层
```xml
<style name="标题类型的字体" parent="TextAppearance.MaterialComponents.Headline4">
   <item name="textAllCaps">true</item>
   <item name="android:textStyle">bold</item>
   <item name="android:textColor">?android:attr/textColorPrimary</item>
</style>
```