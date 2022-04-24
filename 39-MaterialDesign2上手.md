---
toc: false
comments: true
title: MaterialDesign v2 上手
description: 了解使用 MaterialDesign v2 的一些控件
tags:
  - Android
id: 39
categories:
  - Android
date: 2018-6-30
---

部分 MaterialDesign v2 的使用，由于当前网站域名没有做备份的原因，图床云存储不可用导致很多示例图片的地址失效。

<!-- more -->


#### 本文使用环境
- Android Studio3.3
- Kotlin 1.2.50
- Android 28 build
- AndroidX
- 默认创建 Empty Activity


#### 添加 MaterialDesign 组件依赖
```gradle
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version"
implementation 'androidx.appcompat:appcompat:1.0.0-beta01'
implementation 'androidx.constraintlayout:constraintlayout:1.1.2'
implementation 'com.google.android.material:material:1.0.0-beta01'
```


#### 修改 style.xml 文件
- 默认
```xml
<resources>
    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
</resources>
```

- 替换为 MaterialDesign 主题
```xml
<resources>
    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.DarkActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
</resources>
```


## FloatingActionButton  + BottomAppr

- 效果1：
![](http://7xrysc.com1.z0.glb.clouddn.com/md2.png)

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/floatingButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@drawable/ic_favorite_border_black_24dp"
        app:fabSize="normal"
        app:layout_anchor="@id/bottomAppBar" />

    <com.google.android.material.bottomappbar.BottomAppBar
        android:id="@+id/bottomAppBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:backgroundTint="@color/colorPrimary"
        app:fabAlignmentMode="center"
        app:navigationIcon="@drawable/ic_format_list_bulleted_black_24dp">

</com.google.android.material.bottomappbar.BottomAppBar>
```

> BottomAppBar 一般都是搭配 Menu 菜单一起使用，与 Menu 的使用方式有两种 

1. 把 BottomAppBar 当作是 ActionBar,然后在 BottomAppBar 上引用添加 Menu 菜单
2. 不替换 ActionBar ,单独设置 Menu


#### 方式1：替换 ActionBar

- 记得修改 style ,去掉默认 ActionBar
```xml
<resources>
    <!-- Base application theme. -->
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.NoActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
</resources>
```

- 创建 menu 文件 res/menu/menu.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    <item
        android:id="@+id/menu1"
        android:icon="@drawable/ic_format_list_bulleted_black_24dp"
        android:title="菜单"
        app:showAsAction="ifRoom" />
</menu>
```

- Activity 替换 ActionBar 和 设置 Menu
```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bottomAppBar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu1 -> {
                Toast.makeText(this, "通知--", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
```


#### 方式2：不替换 ActionBar,单独设置 Menu
- 
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setSupportActionBar(bottomAppBar)

        bottomAppBar.replaceMenu(R.menu.menu)
        bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menu1 -> {
                    Toast.makeText(this, "通知--000", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }
}
```


## BottomAppBar + NavigationView
> 这种方式使用 NavigationView ,官方推荐打开的方式是:从下往上打开，用 BottomSheetDialogFragment 替换原来的 DrawerLayout

![](http://7xrysc.com1.z0.glb.clouddn.com/md2_BottomAppBar_NavigationView.png)

- 创建一个 Fragment 继承 BottomSheetDialogFragment 
```kotlin
class BottomNavigationDrawerFragment : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.navigation_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.navigation_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav1 -> println("1")
                R.id.nav2 -> println("2")
                R.id.nav3 -> println("3")
            }
            true
        }
    }
}
```

- Fragment 布局 res/layout/navigation_view
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:layout_marginEnd="8dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:menu="@menu/bottom_nav_drawer_menu" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

- 绑定到 BottomAppBar 的 NavigationIcon 按钮
```kotlin
//MainActivity.kt
bottomAppBar.setNavigationOnClickListener {
    val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
    bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
}
```

- Drawer Item 的监听
```kotlin
//MainActivity.kt
bottomAppBar.setOnMenuItemClickListener {
    when(it.itemId){
        R.id.menu1 -> Toast.makeText(this, "通知--", Toast.LENGTH_SHORT).show()
    }
    true
}
```

- 这里是 MainActivity.kt 的全部代码
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(bottomAppBar)

	/**
         * 不替换 ActionBar 方式添加 Menu
         */
        bottomAppBar.replaceMenu(R.menu.menu)

		//DrawerToggle 监听
        bottomAppBar.setNavigationOnClickListener {
            val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
            bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
        }
		
		//DrawerItem 的监听
        bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.menu1 -> Toast.makeText(this, "通知--", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }
		
    /**
     * 替换 ActionBar 方式添加 Menu
     * 需要 setSupportActionBar(bottomAppBar)
     */
//  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu, menu)
//        return true
//  }
//
//  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        return when (item?.itemId) {
//            R.id.menu1 -> {
//                Toast.makeText(this, "通知--", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//  }
}
```


## Button

- 效果：
![](http://7xrysc.com1.z0.glb.clouddn.com/md2_botton.png)

```xml
    <com.google.android.material.button.MaterialButton
        android:id="@+id/button1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="start"
        android:text="注册"
        app:backgroundTint="#8c00ff"
        app:cornerRadius="16dp"
        app:icon="@drawable/ic_send_black_24dp" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/button2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center_horizontal"
        android:text="注册"
        android:textColor="#8c00ff" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/button3"
        style="@style/Widget.MaterialComponents.Button.TextButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="end"
        android:text="注册"
        app:backgroundTint="#8c00ff" />
```

或者给普通 Button 引用 MaterialDesign 的 style

```xml
    <Button
        style="@style/Widget.MaterialComponents.Button.TextButton"
        android:id="@+id/button1"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="注册" />
```


## Chip

- 效果：
![](http://7xrysc.com1.z0.glb.clouddn.com/md2_chip.png)

```xml
 <com.google.android.material.chip.ChipGroup
            android:id="@+id/chipGroup"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_gravity=""
            android:layout_marginTop="16dp"
            android:background="#ffffff">

            <com.google.android.material.chip.Chip
                android:id="@+id/chip1"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="8dp"
                android:text="衣服"
                android:textColor="#000" />

            <com.google.android.material.chip.Chip
                android:id="@+id/chip2"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="8dp"
                android:text="裤子"
                android:textColor="#000"
                app:closeIconEnabled="true" />

            <com.google.android.material.chip.Chip
                android:id="@+id/chip3"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="8dp"
                android:text="鞋子"
                android:textColor="#000"
                app:chipBackgroundColor="@color/colorPrimary" />

            <com.google.android.material.chip.Chip
                android:id="@+id/chip4"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_margin="8dp"
                android:text="鞋子"
                android:textColor="#000"
                app:chipIcon="@drawable/ic_notifications_black_24dp" />

            <com.google.android.material.chip.Chip
                android:id="@+id/chip5"
                android:layout_width="100dp"
                android:layout_height="wrap_content"
                android:layout_margin="8dp"
                android:text="鞋子"
                android:textColor="#fff"
                app:chipBackgroundColor="#8c00ff" />
        </com.google.android.material.chip.ChipGroup>
```


## MaterialCardView

![](http://7xrysc.com1.z0.glb.clouddn.com/md2_CardVIew.png)

```xml
 <com.google.android.material.card.MaterialCardView
     android:layout_width="wrap_content"
     android:layout_height="wrap_content"
     android:layout_margin="8dp"
     app:strokeColor="#8c00ff"
     app:strokeWidth="2dp">
     <TextView
         android:layout_width="wrap_content"
         android:layout_height="wrap_content"
         android:layout_margin="8dp"
         android:text="材料设计V2部分例子,材料设计V2部分例子材料设计 \n V2部分例子材料设计V2部分例子"
         android:textAlignment="center"
         android:textAppearance="@style/TextAppearance.AppCompat.Title" />
 </com.google.android.material.card.MaterialCardView>
```




















