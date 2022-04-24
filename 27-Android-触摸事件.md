---
toc: false
comments: true
title: Android View 触摸事件传递完结篇
description: 再入 Android View 点击事件传递坑。
tags:
  - Android
id: 29
categories:
  - Android
date: 2017-12-9
---

## 事件的分发顺序

屏幕硬件(底层驱动) -> native -> ViewRootImpl -> Activity

- 知识点1：
  事件序列：点击屏幕产生的一系列事件，这个事件序列中可能包含 down 、move 、up 等。Android 会分别对这个事件序列中的某一个事件进行处理，每次分发一个事件，比如先分发 down, 等 down 被处理完，然后再分发 move。





- 知识点2：
  事件的派送都是外层到里层由各层的 dispatchOnTouchEvent 执行, 如果某个事件从树根到最上面的树叶都没有被消费,  则该事件最后按原路 onTouchEvent 返回, 最终回到传到 Activity 的 onTouchEvent 消费





- 知识点3：
  
  假如我们点击一个 TextView:
  点击事件 onClick 是一个的事件序列的，里面包含了 down 和 up,  如果我们让 textView.setOnTouchListener onTouch 返回 true)，那么这个 textView 回调 onClick 的条件就不成立, 也就是 setOnClickListener 不会被回调； 意味着 Down 事件没有被系统消费,  之后系统下发 move 事件,  如果 move 也被开发者消费, , 那么系统最后发 up, up 也是一路绿灯至 onThouch 。这样就完成了整个事件序列的分发和消费  ; onClickListener 是系统自带消费点击事件的回调,  onTouchEvent 和 onTouch 是给开发者自己实现消费事件的接口,  



        如果返回 setOnTouchListener 的 onTouch -> true 就意味开发者自己接管事件消费, 而此时系统自带的 onClickListener 就收不到事件了, 总之 setOnTouchListener 是 setOnClickListener 的爸爸. onTouch 是 onTouchEvent 的爸爸





        但如果上面我们 setOnTouchListener 的 onTouch-> false 时, 意味着, 当 down 事件发送到当前 TextView 的 onTouch, down 遇到你设置的是 false , 也就是开发者的你不打算理会 down, down 就转身和 onClick 在一起了, 之后的 move 和 up 也不会再经过你的 setOnTouchListener 中的 onTouch, 因为你告诉系统你作为开发者你不打算消费这个事件, 你只要消费了Down , 系统才会给你发后续的其他事件.






- 知识点5:
  默认情况下, 从事件消费事件的角度来看, 子 View 的优先比父 View 高, 比如在两层完全重叠的 ViewGroup 设置 onClick , 只有里面的 ViewGroup 的 onClick 会被调用. 但是从事件接收的顺序来看, 则是父 View 的 dispatchOnTouchEvent 优先获取到事件





- 知识点6:
  View 的焦点优先级高于 ViewGroup



<!-- more -->

## 方法介绍:



- **boolean dispatchTouchEvent(MotionEvent ev) {}**

> 事件分发方法，View 和 ViewGroup 都有此方法  (ViewGroup 继承自 View)

结果返回 false 时：说明当前 View 或 ViewGroup 不向下分发该事件了, 可能自己消耗事件也可能自己不消耗该事件, 反正就是不分发→_→，然后准备接收下一个事件。

结果返回 true 时：将该事件继续往下或往上分发(往上或往下取决于当前的 View 或 ViewGroup 是否是 VIew 树中最后一个视图，如果是最后一个视图则往上返回分发传递)

----------

- **boolean onInterceptTouchEvent(MotionEvent event){}**

> 拦截事件方法，ViewGroup 中特有的方法，View 中没有。

结果返回 true 时：说明 ViewGroup 拦截该事件

结果返回 false 时：说明 ViewGroup 不拦截该事件（Android 大部分 ViewGroup 默认不拦截任何事件）

----------

- **boolean onTouch(View v, MotionEvent event) {}**

> 只有设置了 OnTouchListener 时才会被调用到, 当设置了 OnTouchListener 且 onTouch 返回 false 时, onTouch 会比 onTouchEvent 先执行

结果返回 true 时：dispatchTouchEvent 直接返回 true（前提是一定要实现了 OnTouchListener 这个接口），此时后面的 onTouchEvent 一定不会被调用。（返回 true ，意味着该 view 的 onTouchEvent() 不会被调用，因为 onTouchEvent() 不会被调用，所以 onClick 也不会被调用）

结果返回 false 时：onTouchEvent 一定被调用。

常用法1: 一般这个方法用于对某一个 view 或 viewgroup 的触摸事件监听，比如布局里面有很多 view 和 viewgroup ,当 down 事件传到该 view 的时候，我们需要修改某些变量的值。

常用法2: 决定该 view 或 viewgroup 的 onClick 能否被调用

----------

- **boolean onTouchEvent(MotionEvent event){}**

> View.java 类里面的方法(onTouch 则是 View 里面内部接口的一个抽象方法), 常用的 onClick() 回调就是在这个 onTouchEvent 方法里面被调用的， onClick 只在 up 时会调用，View 和 ViewGroup 都有此方法; onTouchEvent 也决定 dispatchTouchEvent 的返回值，正常情况下 dispatchTouchEvent 的返回值与 onTouchEvent 返回值相同。

结果返回 true 时：说明这个事件被消耗处理掉了，然后代码执行会回到 dispatchTouchEvent 分发方法，让 dispatchTouchEvent 分发方法最终也返回 true, 这样其它的 View 和 ViewGroup 的 onTouchEvent 都不会再被调用。(View 该方法默认返回 true)

结果返回 false 时：说明不消耗该事件，然后回到 dispatchTouchEvent 方法，让 dispatchTouchEvent 方法返回 false，同时该 View 是 ViewTree 中最后一个 View ,则会把该事件返回给父 View, 最终返回给 Activity 处理，如果不是最后一个 View , 则继续向下分发该事件，所以其它的 View 或 ViewGroup 的 onTouchEvent 可能会被调用。

当你自定义 View 的时候，你想让你的 View 响应 onClick ，只需重写 onTouchEvent 方法，让其直接 return super.onTouchEvent(event)； 或者在 return ture 之前调用一遍 super.onTouchEvent(event)。

当 dispatchTouchEvent 在进行事件分发的时候，只有前一个 action 返回 true，才会触发下一个 action（也就是说 dispatchTouchEvent 返回 true 才会进行下一次 action 派发）, 拿点击事件举例子， 点击一个按钮， 事件序列通常是先 down ，然后是 up , 因为点击事件的 onClick 方法是在 up 时触发的，所以在处理 down 的时候，onTouchEvent 必须返回 true， 这样才能执行继续处理后面的 up, 不然 up 事件永远不会被分发下来。

## dispatchTouchEvent 分析

- ViewGroup.dispatchTouchEvent()
1. 当 dispatchTouchEvent 收到 down 事件时候会把上一个事件序列清空, 因为 down 的到来意味着这是一个新的触摸操作, 所以需要重置清空上一个触摸操作的状态；
2. 检查当前自身 ViewGroup 是否被设置拦截
3. 遍历 ViewGroup 里面的所有 View , 通过调用 childView.dispatchTouchEvent(event) 把事件传递到子 View 的 dispatchTouchEvent, 当发现某个 View 的 onTouchEvent 返回 true 时, 停止遍历; 其他 View 将不会收到该触摸操作的所有事件;  如果遇到的是 ViewGroup 就进入到子 ViewGroup 的 dispatchTouchEvent() 则继续遍历



- ViewGroup 拦截了 down 事件 + 自己没消耗 down 事件的情况:
  activity 的 dispatchTouchEvent ( dispatch-A) 把 down 事件发送到 ViewGroup 的 dispatchTouchEvent ( dispatch-VG); dispatch-VG 中会调用 onTntercept-VG 来检测该 VG 是否设置了拦截, 如果 onIntercept-VG 返回 true ,也就是拦截了 down 事件, 那么回把这个事件就会从 dispatch-VG 传到 onTouchEvent-VG 中, 如果 onTouchEvent-VG 返回 false (不消耗), 那么 dispatch-VG 的最终返回结果就是 false, 这就意味着该事件会原路返回到 dispatch-A , 再在 dispatch-A 中传到 onTouchEvent-A, 最后由 onTouchEvent-A 返回true 消耗结束该 down 事件; 因为这个 ViewGroup 拦截了 down 事件, 但没有消耗 down 事件, 所以这个 ViewGroup 的 dispatchTouchEvent 和 onTouchEvent 都不会再收到后面的 move 或者 up 事件了.



- ViewGroup 拦截了 down 事件 + 自己消耗了 down 事件的情况:
  onIntercept-VG 拦截了 down , onTouchEvent-VG 消耗了 down , 那么后续的 move 或者 up 都不会再走 onIntercept-VG),  最后 onTouchEvent-VG 会收到 move 或者 up



- View 消耗事件
  从 View 类的 dispatchTouchEvent 中可以知道, 有两种情况会消费事件,让事件在 view 这结束:
1. view 是可点击的并且设置实现了 onTouchListener 回调
2. view 是可点击的且 onTouchEvent 返回 true + onClickLinstener
