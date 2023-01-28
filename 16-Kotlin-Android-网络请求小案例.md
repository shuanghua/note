---
title: Kotlin Android 网络请求
tags:
  - Android
id: 18
categories:
  - Android
toc: true
comments: true
description: Retrofit 请求新闻热点数据，显示到 RecyclerView 上。
date: 2017-8-31
---

尝试用 kotlin + retrofit 来实现 Android 网络请求 


<!-- more -->

# 新建一个 Android 工程
- 这里我选用的是 AS3.0 beta 版本
- 选中添加对 Kotlin 的支持
- 这里我选择带 BottomNavigationView 的 Activity
- 工程建完后，自动为我们添加了 Kotlin extensions 的插件

---

# Gradle

```gradle
implementation 'com.squareup.retrofit2:retrofit:2.3.0'
implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
```

---

# 布局代码

- 记住我们的 id 就行

```xml

<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="com.shuanghua.kandroid_demo1.MainActivity">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_marginTop="8dp"
        app:layout_constraintBottom_toTopOf="@id/navigation"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <android.support.design.widget.BottomNavigationView
        android:id="@+id/navigation"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:background="?android:attr/windowBackground"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:menu="@menu/navigation" />

</android.support.constraint.ConstraintLayout> 
```
 


---


# JavaBean 和 DataClass

 > DataClass 自带 toString get set 方法。
 

 重点来了，怎么把 Java Bean 变成 kotlin 的 Data Class ？

- 先通过 GsonFormat 来生成 Java Bean


> 然后不惊讶的看着这个老长老长的 Java Bean, 有 120 行，因为太长就不贴代码了。


- 然后将 Java 文件转换成 Kotlin 文件

![](http://7xrysc.com1.z0.glb.clouddn.com/javabeanAndKotlinBean.png)

> 右边是120 行的 Java Bean , 左边是 转换后的 Kotlin Class ,但还不是 Data Class


- 现在我们手动将 Kotlin Class 变量常量啥的放到构造方法里，就复制加个逗号完事

![](http://7xrysc.com1.z0.glb.clouddn.com/DataClass.png)

> 开始的 120 行左右代码就变成了现在的不到 20 行，也可以用内部类嵌套的形式，这里是单独类，具体看项目需求了，例如 Realm 数据库框架就不支持内部类的的形式。


## DataClass 使用总结

- DataClass 默认没有无参的构造函数，在使用一些数据库框架的时候可能会出错。 

- 将 DataClass 构造函数中的参数进行赋初始值，在编译后会生成无参的构造函数。因为时编译后，只能通过反射去调用该构造函数。

- DataClass 编译生成的字节类为 final。


> 解决：使用 Kotlin 官方两个插件 + 注解

- all-open
 
- no-arg

具体使用看官网

[https://kotlinlang.org/docs/reference/compiler-plugins.html](https://kotlinlang.org/docs/reference/compiler-plugins.html "All-open No-arg")

# Retrofit And Gson

```Kotlin

object HttpService {
    val service by lazy {
        val retrofit = Retrofit.Builder()
                .baseUrl(Const.URL_BASE)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofit.create(Service::class.java)
    }
}

interface Service {
    @GET("1071-1")
    fun getData(@Query("showapi_appid") showapi_appid: String, @Query("showapi_sign") showapi_sign: String): Call<NewsBean>
}

```

# Adapter

```Kotlin

class NewsAdapter(private val context: Context) : RecyclerView.Adapter<NewsAdapter.MyViewHolder>() {

    private var list: List<ListBean>? = null

    fun setData(data: List<ListBean>) {
        list = data
        notifyDataSetChanged()
    }

    override fun getItemCount() = list!!.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemContent?.text = list!![position].title
        holder.itemTime?.text = list!![position].day
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int)
            = MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_news, parent, false))

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        val itemContent = itemView?.findViewById<TextView>(R.id.itemContent)
        val itemTime = itemView?.findViewById<TextView>(R.id.itemTime)
    }
}

```

# MainActivity

```Kotlin

class MainActivity : AppCompatActivity() {
    var adapter: NewsAdapter? = null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setRecyclerView()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        HttpService.service.getData(Const.APP_ID, Const.SECRET).enqueue(object : Callback<NewsBean> {
            override fun onFailure(call: Call<NewsBean>?, t: Throwable?) {
            }

            override fun onResponse(call: Call<NewsBean>?, response: Response<NewsBean>?) {
                response?.body()?.showapi_res_body?.showapi_res_body?.list!!.let {
                    adapter?.setData(it)
                }
            }
        })
    }

    private fun setRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        val list = listOf(ListBean("", "", ""))
        adapter = NewsAdapter(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter
        adapter?.setData(list)
    }
}


```