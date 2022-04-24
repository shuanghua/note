---
toc: false
comments: false
title: Fragment 坑爹泄漏笔记
description: 不可见的 Fragment 的成员变量持有 View ,导致整个布局不能被 GC 回收，最终出现内存泄漏
tags:
  - Android
id: 48
categories:
  - Android
date: 2018-12-29
---

---
 
不可见的 Fragment 的成员变量持有 View ,导致整个布局不能被 GC 回收，最终出现内存泄漏

<!-- more -->

代码例子：
环境条件：DataBinding + Navigation + Fragment

```kotlin
class LeakFragment : Fragment(){

    // 成员变量
    private lateinit var binding: FragmentLeakBinding

    // 成员变量
    private lateinit var adapter: RecyclerViewAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLeakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
		binding.recyclerView.adapter = adapter
    }
}
```

上面的代码平常看起来没啥问题，但是当你使用了 Navigation 来导航到别的Fragment 时，内存泄漏就出来了，这里我们假设第二个 Fragment 名字叫 FragmentB.

当我们从 LeakFragment 导航到 FragmentB 的时候，此时 LeakFragment 不可见，LeakFragment 会和布局View 进行分离解绑，解绑之后 GC 会回收 布局View,但不会回收 LeakFragment。所以 LeakFragment 一直会在内存中，LeakFragment 一直在内存中，那么其内部的全局变量 adapter 也会在内存中。现在问题来了，本要被 GC 回收的 布局View 持有了该 adapter（一个生命周期短的持有了生命周期长的实例），结果就可想而知了。


- 解决的办法大概有两种，
1 导航跳转时，让 binding.recyclerview.adapter = null
2 让布局 View 持有的全局对象替换成局部对象，这样就不会造成生命周期不一致的情况

第一种方法在 View 数量比较少的时候可以使用，多的话就不推荐，因为要写很多个 = null
第二种方法比较推荐，我们直接使用 kotlin 的 get() = 这么一个局部对象的获取函数.
```
private val binding: FragmentLeakBinding
        get() = 
```

具体代码：
```kotlin
class LeakFragment : Fragment(){
	//不了解 kotlin 的同学可能会疑问，这不是全局对象吗？上面怎么说是局部对象【请自己查看编译后的 kotlin 字节码对应的 java 代码】
    private val binding: FragmentLeakBinding
        get() = view?.tag as FragmentLeakBinding
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLeakBinding.inflate(inflater, container, false)

		//----------------画重点----------------------------
        binding.root.tag = binding
		//-------------------------------------------------

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
		val adapter = RecyclerViewAdapter()
        binding.recyclerView.adapter = adapter
    }

	override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
		//后续使用 adapter 时
		val adapter = binding.recyclerView.adapter as RecyclerViewAdapter
		adapter.setData()
	}
}
```

tag 解释，很多人会问，为什么要多设置一个 tag ，因为这是 Fragment ,你无法直接把 val binding = FragmentLeakBinding.inflate(inflater, container, false) 这段代码用 get() = 去获取呀。


## activity.setSupportActionBar(toolbar) 导致 Fragment 泄漏
 FragmetA 中没有设置 OptionsMenu , FragmentB 中设置了  setHasOptionsMenu(true) ，并且 ctivity.setSupportActionBar(toolbar) set 一个 Toolbar, 重写了 onCreateOptionsMenu。

 > 泄漏发生步骤： FragmentA 跳转到 FragmentB，然后点击返回到 FragmentA，FragmentB 出现了泄漏

 > 原因分析1：  Activity 持有 FragmentManangerImpl 这个类，这个 FragmentManangerImpl 类中 的 mCreatedMenus 持有我们的 FragmentB ，最终导致 FragmentB 出现了泄漏，那么只需想办法把泄漏的 FragmentB 从这个集合中移除掉，很幸运，我们能很快的通过具体的代码分析找到关键的移除代码

> FragmentManangerImpl : 与 Activity 相关的 Fragment 管理器类

- FragmentManangerImpl -> dispatchCreateOptionsMenu
 ```java
     public boolean dispatchCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (mCurState < Fragment.CREATED) {
            return false;
        }
        boolean show = false;
        ArrayList<Fragment> newMenus = null;
        for (int i = 0; i < mAdded.size(); i++) {
            Fragment f = mAdded.get(i);
            if (f != null) {
                if (f.performCreateOptionsMenu(menu, inflater)) {
                    show = true;
                    if (newMenus == null) {
                        newMenus = new ArrayList<Fragment>();
                    }
                    newMenus.add(f);
                }
            }
        }

        //--------------------------------------------------------------
        if (mCreatedMenus != null) {
            for (int i=0; i<mCreatedMenus.size(); i++) {
                Fragment f = mCreatedMenus.get(i);
                if (newMenus == null || !newMenus.contains(f)) {
                    f.onDestroyOptionsMenu();
                }
            }
        }

        mCreatedMenus = newMenus;
         //--------------------------------------------------------------

        return show;
    }
 ```
> 原因分析2：从上面代码可以知道，mCreatedMenus = newMenus 这个替换的操作能清掉原来泄漏的 FragmentB, 前提是 newMenus 里面的 Fragment 不能和 FragmentB 相同，而且 dispatchCreateOptionsMenu() 这个方法能被调用到。我们通过 查看方法调用查看面板，清楚的知道在 FragmentActivity 中的 onCreatePanelMenu() 这个方法可以触发调用到 dispatchCreateOptionsMenu() 

> 综合： Activity 持有了 Menu , Menu 持有 Toolbar ,最终 Fragment 泄漏


> 解决方法1：让 Fragment 来持有 Menu,也就是说创建一个属于 Fragment 的 Menu, 这样他们的生命周期就是一样的了
```kotlin
val toolbar = view?.findViewById(R.id.toolbar)
toolbar.inflateMenu(R.menu.search_menu)
```


> 解决方法2：继续让 Activity 来持有 Menu, 但必须让 dispatchCreateOptionsMenu 能被调用到，也就说 FragmentA 也必须要设置 setHasOptionsMenu(true)
```kotlin
setHasOptionsMenu(true)
activity.setSupportActionBar(null)
(activity as Activity).onCreatePanelMenu(0, null)
```
