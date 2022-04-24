---
toc: true
comments: true
description: Fragment 创建和传值
title: Fragment 创建和传值
tags:
  - Android
id: 21
categories:
  - Android
date: 2017-9-16
---

# 生成 Fragment

## 反射

- 传一个 Fragment 的 Class 类型,然后通过其名字反射来生成。
```Kotlin
fun creatFragment(clazz: Class<*>): Fragment{
	val className = clazz.name
	fragment = Class.forName(className).newInstance() as Fragment
}
```

## 静态回传 + （Activity 传值给 Fragment）

- 在 Activity 中调用在 Fragment 中的以下函数。

```Kotlin
 companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        /**
         * Activity sends data to Fragment
         */
        fun newInstance(data1: String, data2: String): VideoFragment {
            val fragment = VideoFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, data1)
            args.putString(ARG_PARAM2, data2)
            fragment.arguments = args
            return fragment
        }

		// 注意：不需要传数据，是很不推荐下面这么写，请通过无参数构造函数生成 Fragment
		//fun newInstance(): VideoFragment {
        //    val fragment = VideoFragment()
        //    return fragment
        //}
}
```
- 需要传数据给 Fragment 时：请调用 companion object 中的 newInstance(有参数) 函数。

- 当不需要传数据时，请必须使用无参数的构造函数。

# Fragment 传值给 Activity

## 接口回调

在 Fragment 中定义要给接口，让 Activity 实现这个接口，同过这个接口将 "数据值" 传给 Activity。